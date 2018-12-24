/* @(#)IFFParser.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.iff;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import org.monte.media.exception.AbortException;
import org.monte.media.exception.ParseException;

/**
 * Interprets IFF streams.
 *
 * <p>
 * <b>Abstract</b>
 * <p>
 * "EA IFF 85" is the standard interchange file format on Commodore Amiga
 * Computers. An IFF File is built up of primitive data types, local chunks and
 * group chunks.
 *
 * <p>
 * The format for primitive data types is the same as used by the Motorola
 * MC68000 processor. The high byte and high word of a number are stored first.
 * All primitives larger than one byte are aligned on even byte addresses
 * relative to the start of the file. Zeros should be stored in all the pad
 * bytes. Characters and strings are usually coded according to ISO/DIS 6429.2
 * and ANSI X3.64-1979.
 *
 * <p>
 * Data objects are built up with information blocks (or C structs) called local
 * chunks. IFF supports the three different kinds called 'data chunk', 'property
 * chunk', 'collection chunk'. <br>Data chunks contain the essential information
 * that build up an object, say the bitmap data of a picture or the text of a
 * document. <br>Property chunks specify attributes for following data chunks. A
 * property chunk essentially says "identifier=value". When designing an object,
 * property chunks do describe context information like the size of an image or
 * the color of the text. Specifying the same property chunk more then once
 * overrides the previous setting. <br>Collection chunks describe a property
 * that can occur multiple times. All occurences of a collection chunk must be
 * collected within the scope of the current group chunk.
 *
 * <p>
 * Group chunks are full fledged selfcontained data objects. A FORM Group stands
 * for a single data object where as a CAT Group stands for an untyped group of
 * data objects. A LIST defines a group very much like CAT but also gives a
 * scope for shared properties (stored in PROP Groups).
 *
 * <p>
 * For more information refer to "Amiga ROM Kernel Reference Manual, Devices,
 * Third Edition, Addison Wesley".
 *
 * <p>
 * <b>Grammar for IFF streams</b>
 * <pre>
 * IFFFile     ::= 'FORM' FormGroup | 'CAT ' CatGroup | 'LIST' ListGroup
 * <br>
 * GroupChunk  ::= FormGroup | CatGroup | ListGroup | PropGroup | IFFStream
 * FormGroup   ::= size FormType { ChunkID LocalChunk [pad] | 'FORM' FormGroup [pad] | 'LIST ' ListGroup  [pad] | 'CAT ' CatGroup [pad] }
 * CatGroup    ::= size CatType { 'FORM' FormGroup [pad] | 'LIST ' ListGroup  [pad] | 'CAT ' CatGroup [pad] }
 * ListGroup   ::= size ListType { 'PROP' PropGroup [pad] } { 'FORM' FormGroup [pad] | 'LIST' CatGroup  [pad] | 'CAT ' ListGroup [pad] }
 * PropGroup   ::= size PropType { ChunkID PropertyChunk [pad] }
 * <br>
 * LocalChunk      ::= DataChunk | CollectionChunk | PropertyChunk
 * DataChunk       ::= size struct
 * CollectionChunk ::= size struct
 * PropertyChunk   ::= size struct
 * <br>
 * size        ::= ULONG
 * FormType    ::= ULONG
 * CatType     ::= ULONG
 * ListType    ::= ULONG
 * PropType    ::= ULONG
 * ChunkID     ::= ULONG
 * pad         ::= a single byte of value 0.
 * struct      ::= any C language struct built with primitive data types.
 * </pre>
 *
 * <p>
 * <b>Examples</b>
 *
 * <p>
 * <b>Traversing the raw structure of an IFF file</b>
 * <p>
 * To traverse the file structure you must first set up an IFFVisitor object
 * that does something useful at each call to the visit method. Then create an
 * instance of IFFParser and invoke the #interpret method.
 *
 * <pre>
 * class IFFRawTraversal
 * .	{
 * .	static class Visitor
 * .	implements IFFVisitor
 * .		{
 * .		...implement the visitor interface here...
 * .		}
 * .
 * .	public static void main(String[] args)
 * .		{
 * .		try	{
 * .			Visitor visitor = new Visitor();
 * .			FileInputStream stream = new FileInputStream(args[0]);
 * .			IFFParser p = new IFFParser();
 * .			p.interpret(stream,visitor);
 * .			stream.close();
 * .			}
 * .		catch (IOException e) { System.out.println(e); }
 * .		catch (InterpreterException e)  { System.out.println(e); }
 * .		catch (AbortedException e)  { System.out.println(e); }
 * .		}
 * .	}
 * </pre>
 *
 * <p>
 * <b>Traversing the IFF file and interpreting its content.</b>
 * <p>
 * Since IFF files are not completely self describing (there is no information
 * that helps differentiate between data chunks, property chunks and collection
 * chunks) a reader must set up the interpreter with some contextual information
 * before starting the interpreter.
 * <p>
 * Once at least one chunk has been declared, the interpreter will only call the
 * visitor for occurences of the declared group chunks and data chunks. The
 * property chunks and the collection chunks can be obtained from the current
 * group chunk by calling #getProperty or #getCollection. <br>Note: All
 * information the visitor can obtain during interpretation is only valid during
 * the actual #visit... call. Dont try to get information about properties or
 * collections for chunks that the visitor is not visiting right now.
 *
 * <pre>
 * class InterpretingAnILBMFile
 * .	{
 * .	static class Visitor
 * .	implements IFFVisitor
 * .		{
 * .		...
 * .		}
 * .
 * .	public static void main(String[] args)
 * .		{
 * .		try	{
 * .			Visitor visitor = new Visitor();
 * .			FileInputStream stream = new FileInputStream(args[0]);
 * .			IFFParser p = new IFFParser();
 * .			p.declareGroupChunk('FORM','ILBM');
 * .			p.declarePropertyChunk('ILBM','BMHD');
 * .			p.declarePropertyChunk('ILBM','CMAP');
 * .			p.declareCollectionChunk('ILBM','CRNG');
 * .			p.declareDataChunk('ILBM','BODY');
 * .			p.interpret(stream,visitor);
 * .			stream.close();
 * .			}
 * .		catch (IOException e) { System.out.println(e); }
 * .		catch (InterpreterException e)  { System.out.println(e); }
 * .		catch (AbortedException e)  { System.out.println(e); }
 * .		}
 * .	}
 * </pre>
 *
 * @see	IFFVisitor
 *
 * @author	Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version $Id$
 */
public class IFFParser
        extends Object {

    /**
     * ID for FORMGroupExpression.
     */
    public final static int ID_FORM = 0x464f524d;
    /**
     * ID for CATGroupExpression.
     */
    public final static int ID_CAT = 0x43415420;
    /**
     * ID for CATGroupExpression.
     */
    public final static int ID_LIST = 0x4c495354;
    /**
     * ID for PROPGroupExpression.
     */
    public final static int ID_PROP = 0x50524f50;
    /**
     * ID for unlabeled CATGroupExpressions.
     */
    public final static int ID_FILLER = 0x20202020;
    /**
     * The reserved group IDs "LIST", "FORM", "PROP", "CAT ", " ", "LIS1"
     * through "LIS9", "FOR1" through "FOR9" and "CAT1" through "CAT9" may not
     * be used for type IDs and for local chunk IDs.
     */
    public final static int[] RESERVED_IDs = {
        0x4c495354, 0x464f524d, 0x50524f50, 0x43415420, 0x20202020,
        0x4c495331, 0x4c495332, 0x4c495333, 0x4c495334, 0x4c495335, 0x4c495336, 0x4c495337, 0x4c495338, 0x4c495339,
        0x464f5231, 0x464f5232, 0x464f5233, 0x464f5234, 0x464f5235, 0x464f5236, 0x464f5237, 0x464f5238, 0x464f5239,
        0x43415431, 0x43415432, 0x43415433, 0x43415434, 0x43415435, 0x43415436, 0x43415437, 0x43415438, 0x43415439
    };
    /**
     * The visitor traverses the parse tree.
     */
    private IFFVisitor visitor;
    /**
     * List of data chunks the visitor is interested in.
     */
    private HashMap<IFFChunk, IFFChunk> dataChunks;
    /**
     * List of property chunks the visitor is interested in.
     */
    private HashMap<IFFChunk, IFFChunk> propertyChunks;
    /**
     * List of collection chunks the visitor is interested in.
     */
    private HashMap<IFFChunk, IFFChunk> collectionChunks;
    /**
     * List of group chunks the visitor is interested in.
     */
    private HashSet<Integer> groupChunks;
    /**
     * Reference to the input stream.
     */
    private MC68000InputStream in;
    /**
     * Whether to read the data in data chunks or skip it.
     */
    private boolean readData = true;

    /* ---- constructors ---- */
    /**
     * Constructs a new IFF parser.
     */
    public IFFParser() {
    }

    /* ---- accessor methods ---- */
 /* ---- action methods ---- */
    /**
     * Interprets the IFFFileExpression located at the current position of the
     * indicated InputStream. Lets the visitor traverse the IFF parse tree
     * during interpretation.
     *
     * <p>
     * Pre condition
     * <ul><li>	Data-, property- and collection chunks must have been declared
     * prior to this call. <li>	When the client never declared chunks, then all
     * local chunks will be interpreted as data chunks. <li>
     * The stream must be positioned at the beginning of the IFFFileExpression.
     * </ul>
     * <p>
     * Post condition
     * <ul><li>	When no exception was thrown then the stream is positioned after
     * the IFFFileExpression.
     * </ul>
     * <p>
     * Obligation The visitor may throw an ParseException or an AbortException
     * during tree traversal.
     *
     * @exception ParseException Is thrown when an interpretation error occured.
     * The stream is positioned where the error occured.
     * @exception	AbortException Is thrown when the visitor decided to abort the
     * interpretation.
     */
    public void parse(InputStream in, IFFVisitor v)
            throws ParseException, AbortException, IOException {
        this.in = new MC68000InputStream(in);
        visitor = v;
        parseFile();
    }

    /**
     * Parses an IFF-85 file.
     *
     * <pre>
     * IFF = 'FORM' FormGroup | 'CAT ' CatGroup | 'LIST' ListGroup
     * </pre>
     */
    private void parseFile()
            throws ParseException, AbortException, IOException {
        int id = in.readLONG();

        switch (id) {
            case ID_FORM:
                parseFORM(null);
                break;
            case ID_CAT:
                parseCAT(null);
                break;
            case ID_LIST:
                parseLIST(null);
                break;
            default:
                throw new ParseException("IFF-85 files must start with 'FORM', 'CAT ', or 'LIST'. But not with: '" + idToString(id) + "'");
        }
    }

    /**
     * Parses a FORM group chunk.
     * <pre>
     * FormGroup = size FormType { 'FORM' FormGroup [Pad] |
     * 'CAT ' CatGroup  [Pad] |
     * 'LIST' ListGroup [Pad] |
     * ChunkID LocalChunk [Pad] }
     * </pre>
     */
    private void parseFORM(HashMap<Integer, IFFChunk> props)
            throws ParseException, AbortException, IOException {
        long size = in.readULONG();
        if (size == 0) {
            size = in.readINT64();
        }

        long scan = in.getScan();
        int type = in.readLONG();

        if (!isFormType(type)) {
            throw new ParseException("Invalid Form Type: " + idToString(type));
        }

        IFFChunk propGroup = props == null ? null : props.get(type);
        IFFChunk chunk = new IFFChunk(type, ID_FORM, size, scan, propGroup);

        if (isGroupChunk(chunk)) {
            visitor.enterGroup(chunk);
        }

        long finish = scan + size;
        try {
            while (in.getScan() < finish) {
                long idscan = in.getScan();
                int id = in.readLONG();
                switch (id) {
                    case ID_FORM:
                        parseFORM(props);
                        break;
                    case ID_CAT:
                        parseCAT(props);
                        break;
                    case ID_LIST:
                        parseLIST(props);
                        break;
                    default:
                        if (isLocalChunkID(id)) {
                            parseLocalChunk(chunk, id);
                        } else {
                            throw new ParseException("Invalid IFFChunk within FORM: " + idToString(id) + " at offset:" + idscan);
                        }
                }
                //            System.out.println("Found IFFChunk within Form:" + idToString(id)+" at offset:"+idscan);

                in.align();
            }
        } catch (EOFException e) {
            System.err.println("Unexpected EOF after:" + (in.getScan() - scan) + " should be:" + size);
            e.printStackTrace();
        }
        if (isGroupChunk(chunk)) {
            visitor.leaveGroup(chunk);
        }
    }

    /**
     * Parses a CAT group chunk.
     * <pre>
     * CatGroup = size CatType { 'FORM' FormGroup [Pad] |
     * 'CAT ' CatGroup  [Pad] |
     * 'LIST' ListGroup [Pad] }
     * </pre>
     */
    private void parseCAT(HashMap<Integer, IFFChunk> props)
            throws ParseException, AbortException, IOException {
        long size = in.readULONG();
        long scan = in.getScan();
        int type = in.readLONG();

        if (!isContentType(type)) {
            throw new ParseException("Invalid Content Type: " + idToString(type));
        }

        IFFChunk propGroup = props == null ? null : props.get(type);
        IFFChunk chunk = new IFFChunk(type, ID_CAT, size, scan, propGroup);

        if (isGroupChunk(chunk)) {
            visitor.enterGroup(chunk);
        }

        long finish = scan + size;
        while (in.getScan() < finish) {
            int id = in.readLONG();

            switch (id) {
                case ID_FORM:
                    parseFORM(props);
                    break;
                case ID_CAT:
                    parseCAT(props);
                    break;
                case ID_LIST:
                    parseLIST(props);
                    break;
                default:
                    throw new ParseException("Invalid IFFChunk within CAT: " + idToString(id));
            }
            in.align();
        }

        if (isGroupChunk(chunk)) {
            visitor.leaveGroup(chunk);
        }
    }

    /**
     * Parses a LIST group chunk.
     * <pre>
     * ListGroup = size ListType { 'PROP' PropGroup [Pad] } { 'FORM' FormGroup [Pad] |
     * 'CAT ' CatGroup  [Pad] |
     * 'LIST' ListGroup [Pad] }
     * </pre>
     */
    @SuppressWarnings("unchecked")
    private void parseLIST(HashMap<Integer, IFFChunk> props)
            throws ParseException, AbortException, IOException {
        long size = in.readULONG();
        long scan = in.getScan();
        int type = in.readLONG();

        if (!isFormType(type)) {
            throw new ParseException("Invalid Form Type: " + idToString(type));
        }

        IFFChunk propGroup = props == null ? null : props.get(type);
        IFFChunk chunk = new IFFChunk(type, ID_LIST, size, scan, propGroup);

        if (isGroupChunk(chunk)) {
            visitor.enterGroup(chunk);
        }

        props = new HashMap<Integer, IFFChunk>();
        long finish = scan + size;
        while (in.getScan() < finish) {
            int id = in.readLONG();

            switch (id) {
                case ID_FORM:
                    parseFORM(props);
                    break;
                case ID_CAT:
                    parseCAT(props);
                    break;
                case ID_LIST:
                    parseLIST(props);
                    break;
                case ID_PROP:
                    IFFChunk prop = parsePROP();
                    props.put(new Integer(prop.getType()), prop);
                    break;
                default:
                    if (isLocalChunkID(id)) {
                        parseLocalChunk(chunk, id);
                    } else {
                        throw new ParseException("Invalid IFFChunk ID within LIST: " + idToString(id));
                    }
            }
            in.align();
        }

        if (isGroupChunk(chunk)) {
            visitor.leaveGroup(chunk);
        }
    }

    /**
     * Parses a PROP group chunk.
     * <pre>
     * PropGroup   ::= size PropType { ChunkID PropertyChunk [pad] }
     * </pre>
     */
    private IFFChunk parsePROP()
            throws ParseException, AbortException, IOException {
        long size = in.readULONG();
        long scan = in.getScan();
        int type = in.readLONG();

        if (!isFormType(type)) {
            throw new ParseException("Invalid Form Type: " + idToString(type));
        }

        IFFChunk chunk = new IFFChunk(type, ID_PROP, size, scan);

        if (isGroupChunk(chunk)) {
            visitor.enterGroup(chunk);
        }

        long finish = scan + size;
        while (in.getScan() < finish) {
            int id = in.readLONG();

            if (isLocalChunkID(id)) {
                parseLocalChunk(chunk, id);
            } else {
                throw new ParseException("Invalid IFFChunk ID within PROP: " + idToString(id));
            }

            in.align();
        }

        if (isGroupChunk(chunk)) {
            visitor.leaveGroup(chunk);
        }

        return chunk;
    }

    /**
     * Parses a local chunk.
     * <pre>
     * LocalChunk  ::= size { DataChunk | PropertyChunk | CollectionChunk }
     * DataChunk = PropertyChunk = CollectionChunk ::= { byte }*size
     * </pre>
     */
    private void parseLocalChunk(IFFChunk parent, int id)
            throws ParseException, AbortException, IOException {
        long size = in.readULONG();
        long scan = in.getScan();

        IFFChunk chunk = new IFFChunk(parent.getType(), id, size, scan);

        if (isDataChunk(chunk)) {
            if (readData) {
                byte[] data = new byte[(int) size];
                in.read(data, 0, (int) size);
                chunk.setData(data);
            } else {
                in.skipFully(size);
            }
            visitor.visitChunk(parent, chunk);
        } else if (isPropertyChunk(chunk)) {
            if (readData) {
                byte[] data = new byte[(int) size];
                in.read(data, 0, (int) size);
                chunk.setData(data);
            } else {
                in.skipFully(size);
            }
            parent.putPropertyChunk(chunk);
        } else if (isCollectionChunk(chunk)) {
            if (readData) {
                byte[] data = new byte[(int) size];
                in.read(data, 0, (int) size);
                chunk.setData(data);
            } else {
                in.skipFully(size);
            }
            parent.addCollectionChunk(chunk);
        } else {
            if (size > 0) {
                in.skipFully((int) size);
            }
        }
    }

    /**
     * Checks whether the ID of the chunk has been declared as a data chunk.
     *
     * <p>
     * Pre condition
     * <ul><li>	Data chunks must have been declared before the interpretation
     * has been started. <li>	This method will always return true when neither
     * data chunks, property chunks nor collection chunks have been declared,
     * </ul>
     *
     * @param chunk Chunk to be verified.
     * @return True when the parameter is a data chunk.
     */
    protected boolean isDataChunk(IFFChunk chunk) {
        if (dataChunks == null) {
            if (collectionChunks == null && propertyChunks == null) {
                return true;
            } else {
                return false;
            }
        } else {
            return dataChunks.containsKey(chunk);
        }
    }

    /**
     * Checks wether the ID of the chunk has been declared as a group chunk.
     *
     * <p>
     * Pre condition
     * <ul><li>	Group chunks must have been declared before the interpretation
     * has been started. (Otherwise the response is always true).</li>
     * </ul>
     *
     * @param chunk Chunk to be verified.
     * @return True when the visitor is interested in this is a group chunk.
     */
    protected boolean isGroupChunk(IFFChunk chunk) {
        if (groupChunks == null) {
            return true;
        } else {
            return groupChunks.contains(chunk.getID());
        }
    }

    /**
     * Checks wether the ID of the chunk has been declared as a property chunk.
     *
     * <p>
     * Pre condition
     * <ul>
     * <li>	Property chunks must have been declared before the interpretation
     * has been started. </li>
     * <li>	This method will always return false when neither data chunks,
     * property chunks nor collection chunks have been declared.</li>
     * </ul>
     */
    protected boolean isPropertyChunk(IFFChunk chunk) {
        if (propertyChunks == null) {
            return false;
        } else {
            return propertyChunks.containsKey(chunk);
        }
    }

    /**
     * Checks wether the ID of the chunk has been declared as a collection
     * chunk.
     *
     * <p>
     * Pre condition
     * <ul>
     * <li>	Collection chunks must have been declared before the interpretation
     * has been started.</li>
     * <li>	This method will always return true when neither data chunks,
     * property chunks nor collection chunks have been declared.</li>
     * </ul>
     *
     * @param chunk	Chunk to be verified.
     * @return True when the parameter is a collection chunk.
     */
    protected boolean isCollectionChunk(IFFChunk chunk) {
        if (collectionChunks == null) {
            return false;
        } else {
            return collectionChunks.containsKey(chunk);
        }
    }

    /**
     * Declares a data chunk.
     *
     * <p>
     * Pre condition
     * <ul>
     * <li>	The chunk must not have already been declared as of a different
     * type. </li>
     * <li>	Declarations may not be done during interpretation of an
     * IFFFileExpression.</li>
     * </ul>
     *
     * <p>
     * Post condition<ul> <li>	Data chunk declared
     * </ul>
     *
     * @param	type Type of the chunk. Must be formulated as a TypeID conforming
     * to the method #isFormType.
     * @param	id ID of the chunk. Must be formulated as a ChunkID conforming to
     * the method #isLocalChunkID.
     */
    @SuppressWarnings("unchecked")
    public void declareDataChunk(int type, int id) {
        IFFChunk chunk = new IFFChunk(type, id);
        if (dataChunks == null) {
            dataChunks = new HashMap<IFFChunk, IFFChunk>();
        }
        dataChunks.put(chunk, chunk);
    }

    /**
     * Convenience method.
     */
    /*	public void declareDataChunk(String type, String id)
     {
     declareDataChunk(stringToID(type),stringToID(id));
     }
     */
    /**
     * Declares a FORM group chunk.
     *
     * <p>
     * Pre condition
     * <ul>
     * <li>	The chunk must not have already been declared as of a different
     * type. </li>
     * <li>	Declarations may not be done during interpretation of an
     * IFFFileExpression.</li>
     * </ul>
     *
     * <p>
     * Post condition
     * <ul>
     * <li>	Group chunk declared</li>
     * </ul>
     *
     * @param	type Type of the chunk. Must be formulated as a TypeID conforming
     * to the method #isFormType.
     * @param	id ID of the chunk. Must be formulated as a ChunkID conforming to
     * the method #isContentsType.
     */
    public void declareGroupChunk(int type, int id) {
        //IFFChunk chunk = new IFFChunk(type, id);
        if (groupChunks == null) {
            groupChunks = new HashSet<Integer>();
        }
        groupChunks.add(id);
    }

    /**
     * Convenience method.
     */
    /*	public void declareGroupChunk(String type, String id)
     {
     declareGroupChunk(stringToID(type),stringToID(id));
     }
     */
    /**
     * Declares a property chunk.
     *
     * <p>
     * Pre condition
     * <ul>
     * <li>	The chunk must not have already been declared as of a different
     * type. </li>
     * <li>	Declarations may not be done during interpretation of an
     * IFFFileExpression.</li>
     * </ul>
     *
     * <p>
     * Post condition <ul><li>	Group chunk declared
     * </ul>
     *
     *
     * @param	type Type of the chunk. Must be formulated as a TypeID conforming
     * to the method #isFormType.
     * @param	id ID of the chunk. Must be formulated as a ChunkID conforming to
     * the method #isLocalChunkID.
     */
    public void declarePropertyChunk(int type, int id) {
        IFFChunk chunk = new IFFChunk(type, id);
        if (propertyChunks == null) {
            propertyChunks = new HashMap<IFFChunk, IFFChunk>();
        }
        propertyChunks.put(chunk, chunk);
    }

    /**
     * Convenience method.
     */
    /*	public void declarePropertyChunk(String type, String id)
     {
     declarePropertyChunk(stringToID(type),stringToID(id));
     }
     */
    /**
     * Declares a collection chunk.
     *
     * <p>
     * Pre condition 
     * <ul>
     * <li>	The chunk must not have already been declared as
     * of a different type. </li>
     * <li>	Declarations may not be done during
     * interpretation of an IFFFileExpression.</li>
     * </ul>
     *
     * <p>
     * Post condition <ul>
     * <li>	Group chunk declared</li>
     * </ul>
     *
     * @param	type Type of the chunk. Must be formulated as a TypeID conforming
     * to the method #isFormType.
     * @param	id ID of the chunk. Must be formulated as a ChunkID conforming to
     * the method #isLocalChunkID.
     */
    public void declareCollectionChunk(int type, int id) {
        IFFChunk chunk = new IFFChunk(type, id);
        if (collectionChunks == null) {
            collectionChunks = new HashMap<IFFChunk, IFFChunk>();
        }
        collectionChunks.put(chunk, chunk);
    }

    /**
     * Convenience method.
     */
    /*	public void declareCollectionChunk(String type, String id)
     {
     declareCollectionChunk(stringToID(type),stringToID(id));
     }
     */
 /* ---- Class methods ---- */
    /**
     * Checks wether the argument represents a valid IFF GroupID.
     *
     * <p>
     * Validation <ul><li>	Group ID must be one of FORM_ID, CAT_ID, LIST_ID or
     * PROP_ID.
     * </ul>
     *
     * @param	id Chunk ID to be checked.
     * @return	True when the chunk ID is a valid Group ID.
     */
    public static boolean isGroupID(int id) {
        if (id == ID_FORM
                || id == ID_CAT
                || id == ID_LIST
                || id == ID_PROP) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if the argument represents a valid IFF ID.
     *
     * <p>
     * Validation <ul><li>	Every byte of an ID must be in the range of 0x20..0x7e
     * <li>	The id may not have leading spaces (unless the id is a NULL_ID).
     * </ul>
     *
     * @param	id Chunk ID to be checked.
     * @return	True when the ID is a valid IFF chunk ID.
     */
    public static boolean isID(int id) {
        int value1 = id >> 24;
        int value2 = (id >> 16) & 0xff;
        int value3 = (id >> 8) & 0xff;
        int value4 = id & 0xff;

        if (value1 < 0x20 || value1 > 0x7e
                || value2 < 0x20 || value2 > 0x7e
                || value3 < 0x20 || value3 > 0x7e
                || value4 < 0x20 || value4 > 0x7e) {
            return false;
        }

        if (id != ID_FILLER && value1 == 0x20) {
            return false;
        }

        return true;
    }

    /**
     * Returns whether the argument is a valid Local Chunk ID.
     *
     * <p>
     * Validation <ul>
     * <li>	Local Chunk IDs may not be a NULL_ID and may not have
     * leading spaces. <li>	Local Chunk IDs may not collid with GroupIDs.
     * </ul>
     *
     * @param id Chunk ID to be checked.
     * @return	True when the chunk ID is a Local Chunk ID.
     */
    public static boolean isLocalChunkID(int id) {
        if (id == ID_FILLER) {
            return false;
        }
        if (isGroupID(id)) {
            return false;
        }
        return isID(id);
    }

    public static boolean isReservedID(int id) {
        for (int i = 0; i < RESERVED_IDs.length; i++) {
            if (id == RESERVED_IDs[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns wether the argument is a valid FormType.
     *
     * <p>
     * Validation:
     * <ul>
     * <li>	The FORM type is a restricted ID that may not contain
     * lower case letters or punctuation characters. 
     * <li>	FORM type may not
     * collide with GroupID.
     * </ul>
     *
     * @param	id Chunk ID to be checked.
     * @return	True when the chunk ID is a Form Type.
     */
    public static boolean isFormType(int id) {
        if (isReservedID(id)) {
            return false;
        }

        int value1 = id >> 24;
        int value2 = (id >> 16) & 0xff;
        int value3 = (id >> 8) & 0xff;
        int value4 = id & 0xff;

        if (value1 < 0x30 || value1 > 0x5a || (value1 > 0x49 && value1 < 0x41)
                || (value2 < 0x30 && value2 != 0x20) || value2 > 0x5a || (value2 > 0x49 && value2 < 0x41)
                || (value3 < 0x30 && value3 != 0x20) || value3 > 0x5a || (value3 > 0x49 && value3 < 0x41)
                || (value4 < 0x30 && value4 != 0x20) || value4 > 0x5a || (value4 > 0x49 && value4 < 0x41)) {
            return false;
        }

        if (isGroupID(id)) {
            return false;
        }

        return true;
    }

    /**
     * Returns wether the argument is a valid Content Type ID.
     *
     * <p>
     * Validation<ul>
     * <li>	The Content type is a FORM type ID or a NULL_ID</li>
     * </ul>
     *
     * @param	id Chunk ID to be checked.
     * @return	True when the chunk ID is a Contents Type.
     */
    public static boolean isContentType(int id) {
        if (id == ID_FILLER) {
            return true;
        } else {
            return isFormType(id);
        }
    }

    /**
     * Convert an integer IFF identifier to String.
     *
     * @param	anID ID to be converted.
     * @return	String representation of the ID.
     */
    public static String idToString(int anID) {
        byte[] bytes = new byte[4];

        bytes[0] = (byte) (anID >>> 24);
        bytes[1] = (byte) (anID >>> 16);
        bytes[2] = (byte) (anID >>> 8);
        bytes[3] = (byte) (anID >>> 0);

        return new String(bytes);
    }

    /**
     * Converts the first four letters of the String into an IFF Identifier.
     *
     * @param	aString String to be converted.
     * @return	ID representation of the String.
     */
    public static int stringToID(String aString) {
        byte[] bytes = aString.getBytes();

        return ((int) bytes[0]) << 24
                | ((int) bytes[1]) << 16
                | ((int) bytes[2]) << 8
                | ((int) bytes[3]) << 0;
    }

    public boolean isReadData() {
        return readData;
    }

    public void setReadData(boolean readData) {
        this.readData = readData;
    }
}
