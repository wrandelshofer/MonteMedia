/* @(#)RIFFParser.java
 * Copyright © 2005-2012 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.riff;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.WeakHashMap;
import javax.imageio.stream.ImageInputStream;
import ru.sbtqa.monte.media.AbortException;
import ru.sbtqa.monte.media.ParseException;
import ru.sbtqa.monte.media.io.ImageInputStreamAdapter;

/**
 * Interprets Resource Interchange File Format (RIFF) streams.
 *
 * 
 * <b>Abstract</b>
 * 
 * <b>RIFF File Format</b> A RIFF file consists of a RIFF header followed by
 * zero or more lists and chunks.
 * 
 * The RIFF header has the following form:
 * 
 * 'RIFF' fileSize fileType (data)
 *  where 'RIFF' is the literal FOURCC code 'RIFF', fileSize is a 4-byte
 * value giving the size of the data in the file, and fileType is a FOURCC that
 * identifies the specific file type. The value of fileSize includes the size of
 * the fileType FOURCC plus the size of the data that follows, but does not
 * include the size of the 'RIFF' FOURCC or the size of fileSize. The file data
 * consists of chunks and lists, in any order.
 * 
 * <b>FOURCCs</b><br> A FOURCC (four-character code) is a 32-bit unsigned
 * integer created by concatenating four ASCII characters. For example, the
 * FOURCC 'abcd' is represented on a Little-Endian system as 0x64636261. FOURCCs
 * can contain space characters, so ' abc' is a valid FOURCC. The RIFF file
 * format uses FOURCC codes to identify stream types, data chunks, index
 * entries, and other information.
 * 
 * A chunk has the following form:
 * 
 * ckID ckSize ckData
 *  where ckID is a FOURCC that identifies the data contained in the
 * chunk, ckData is a 4-byte value giving the size of the data in ckData, and
 * ckData is zero or more bytes of data. The data is always padded to nearest
 * WORD boundary. ckSize gives the size of the valid data in the chunk; it does
 * not include the padding, the size of ckID, or the size of ckSize.
 * 
 * A list has the following form:
 * 
 * 'LIST' listSize listType listData
 *  where 'LIST' is the literal FOURCC code 'LIST', listSize is a 4-byte
 * value giving the size of the list, listType is a FOURCC code, and listData
 * consists of chunks or lists, in any order. The value of listSize includes the
 * size of listType plus the size of listData; it does not include the 'LIST'
 * FOURCC or the size of listSize.
 * 
 * For more information see:
 * http://msdn.microsoft.com/archive/default.asp?url=/archive/en-us/directx9_c/directx/htm/avirifffilereference.asp
 * http://msdn.microsoft.com/archive/default.asp?url=/archive/en-us/directx9_c/directx/htm/aboutriff.asp
 * 
 * 
 * <b>Grammar for RIFF streams used by this parser</b>
 * 
 * RIFFFile    ::= 'RIFF' FormGroup
 * <br>
 * GroupChunk  ::= FormGroup | ListGroup
 * FormGroup   ::= size GroupType [ ChunkID LocalChunk [pad] | 'LIST ' ListGroup  [pad] }
 * ListGroup   ::= size GroupType [ ChunkID LocalChunk [pad] | 'LIST ' ListGroup  [pad] }
 * <br>
 * LocalChunk      ::= DataChunk | CollectionChunk | PropertyChunk
 * DataChunk       ::= size [ struct ]
 * CollectionChunk ::= size [ struct ]
 * PropertyChunk   ::= size [ struct ]
 * <br>
 * size        ::= ULONG
 * GroupType   ::= FourCC
 * ChunkID     ::= FourCC
 * pad         ::= (BYTE)0
 * struct      ::= any C language struct built with primitive data types.
 * 
 *
 * 
 * <b>Examples</b>
 *
 * 
 * <b>Traversing the raw structure of a RIFF file</b>
 * 
 * To traverse the file structure you must first set up a RIFFVisitor object
 * that does something useful at each call to the visit method. Then create an
 * instance of a RIFFParser and invoke the #interpret method.
 *
 * 
 * class RIFFRawTraversal
 * .	{
 * .	static class Visitor
 * .	implements RIFFVisitor
 * .		{
 * .		...implement the visitor interface here...
 * .		}
 * .
 * .	public static void main(String[] args)
 * .		{
 * .		try	{
 * .			Visitor visitor = new Visitor();
 * .			FileInputStream stream = new FileInputStream(args[0]);
 * .			RIFFParser p = new RIFFParser();
 * .			p.interpret(stream,visitor);
 * .			stream.close();
 * .			}
 * .		catch (IOException e) { System.out.println(e); }
 * .		catch (InterpreterException e)  { System.out.println(e); }
 * .		catch (AbortedException e)  { System.out.println(e); }
 * .		}
 * .	}
 * 
 *
 * 
 * <b>Traversing the RIFF file and interpreting its content.</b>
 * 
 * Since RIFF files are not completely self describing (there is no information
 * that helps differentiate between data chunks, property chunks and collection
 * chunks) a reader must set up the interpreter with some contextual information
 * before starting the interpreter.
 * 
 * Once at least one chunk has been declared, the interpreter will only call the
 * visitor for occurences of the declared group chunks and data chunks. The
 * property chunks and the collection chunks can be obtained from the current
 * group chunk by calling #getProperty or #getCollection. <br>Note: All
 * information the visitor can obtain during interpretation is only valid during
 * the actual #visit... call. Dont try to get information about properties or
 * collections for chunks that the visitor is not visiting right now.
 *
 * 
 * class InterpretingAnILBMFile
 * .	{
 * .	static class Visitor
 * .	implements RIFFVisitor
 * .		{
 * .		...
 * .		}
 * .
 * .	public static void main(String[] args)
 * .		{
 * .		try	{
 * .			Visitor visitor = new Visitor();
 * .			FileInputStream stream = new FileInputStream(args[0]);
 * .			RIFFParser p = new RIFFParser();
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
 * 
 *
 * @see	RIFFVisitor
 *
 * @author	Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version $ID$
 */
public class RIFFParser extends Object {

    private final static boolean DEBUG = false;
    /**
     * ID for FormGroupExpression.
     */
    public final static int RIFF_ID = stringToID("RIFF");
    /**
     * ID for ListGroupExpression.
     */
    public final static int LIST_ID = stringToID("LIST");
    /**
     * ID for NULL chunks.
     */
    public final static int NULL_ID = stringToID("    ");
    /**
     * ID for NULL chunks.
     */
    public final static int NULL_NUL_ID = stringToID("\0\0\0\0");
    /**
     * ID for JUNK chunks.
     */
    public final static int JUNK_ID = stringToID("JUNK");
    /**
     * The visitor traverses the parse tree.
     */
    private RIFFVisitor visitor;
    /**
     * List of data chunks the visitor is interested in.
     */
    private HashSet<RIFFChunk> dataChunks;
    /**
     * List of property chunks the visitor is interested in.
     */
    private HashSet<RIFFChunk> propertyChunks;
    /**
     * List of collection chunks the visitor is interested in.
     */
    private HashSet<RIFFChunk> collectionChunks;
    /**
     * List of stop chunks the visitor is interested in.
     */
    private HashSet<Integer> stopChunkTypes;
    /**
     * List of group chunks the visitor is interested in.
     */
    private HashSet<RIFFChunk> groupChunks;
    /**
     * Reference to the input stream.
     */
    private RIFFPrimitivesInputStream in;
    /**
     * Reference to the image input stream.
     */
    private ImageInputStream iin;
    /**
     * Whether we stop at all chunks.
     */
    private boolean isStopChunks;
    /**
     * Stream offset.
     */
    private long streamOffset;
    /**
     * Whether to read the data in data chunks or skip it.
     */
    private boolean readData = true;

    /* ---- constructors ---- */
    /**
     * Constructs a new RIFF parser.
     */
    public RIFFParser() {
    }

    public long getStreamOffset() {
        return streamOffset;
    }

    public void setStreamOffset(long offset) {
        this.streamOffset = offset;
    }

    /* ---- accessor methods ---- */
 /* ---- action methods ---- */
    /**
     * Interprets the RIFFFile expression located at the current position of the
     * indicated InputStream. Lets the visitor traverse the RIFF parse tree
     * during interpretation.
     *
     * 
     * Pre condition Data-, property- and collection chunks must have been
     * declared prior to this call. When the client never declared chunks, then
     * all local chunks will be interpreted as data chunks. The stream must be
     * positioned at the beginning of the RIFFFileExpression.
     *
     * 
     * Post condition When no exception was thrown then the stream is positioned
     * after the RIFFFile expression.
     *
     * 
     * Obligation The visitor may throw an ParseException or an AbortException
     * during tree traversal.
     *
     * @param in TODO
     * @param v TODO
     * @return TODO
     * @exception ParseException Is thrown when an interpretation error occured.
     * The stream is positioned where the error occured.
     * @exception	AbortException Is thrown when the visitor decided to abort the
     * interpretation.
     * @throws java.io.IOException TODO
     */
    public long parse(InputStream in, RIFFVisitor v)
          throws ParseException, AbortException, IOException {
        this.in = new RIFFPrimitivesInputStream(in);
        visitor = v;
        parseFile();
        return getScan(this.in);
    }

    public long parse(ImageInputStream in, RIFFVisitor v)
          throws ParseException, AbortException, IOException {
        return parse(new ImageInputStreamAdapter(in), v);
    }

    /**
     * Parses a RIFF file.
     *
     * 
     * RIFF = 'RIFF' FormGroup
     * 
     */
    private void parseFile()
          throws ParseException, AbortException, IOException {
        int id = in.readFourCC();

        if (id == RIFF_ID) {
            parseFORM(null);
        } else if (id == JUNK_ID) {
            parseLocalChunk(null, id);
        } else if (iin != null) {
            throw new ParseException("Invalid RIFF File ID: \"" + idToString(id) + " 0x" + Integer.toHexString(id) + " near " + iin.getStreamPosition() + " 0x" + Long.toHexString(iin.getStreamPosition()));
        } else {
            throw new ParseException("Invalid RIFF File ID: \"" + idToString(id) + " 0x" + Integer.toHexString(id));
        }
    }

    private long getScan(RIFFPrimitivesInputStream in) {
        return in.getScan() + streamOffset;
    }

    /**
     * Parses a FORM group.
     * 
     * FormGroup ::= size GroupType { ChunkID LocalChunk [pad]
     * | 'FORM' FormGroup  [pad] }
     * | 'LIST' ListGroup  [pad] }
     * 
     */
    private void parseFORM(HashMap<Integer, RIFFChunk> props)
          throws ParseException, AbortException, IOException {
        long size = in.readULONG();
        long offset = getScan(in);
        int type = in.readFourCC();
        if (DEBUG) {
            System.out.println("RIFFParser.parseForm " + idToString(type));
        }
        if (!isGroupType(type)) {
            throw new ParseException("Invalid FORM Type: \"" + idToString(type) + "\"");
        }

        RIFFChunk propGroup = (props == null) ? null : props.get(type);
        RIFFChunk chunk = new RIFFChunk(type, RIFF_ID, size, offset, propGroup);

        boolean visitorWantsToEnterGroup = false;
        if (isGroupChunk(chunk) && (visitorWantsToEnterGroup = visitor.enteringGroup(chunk))) {
            visitor.enterGroup(chunk);
        }

        try {
            long finish = offset + size;
            while (getScan(in) < finish) {
                long idscan = getScan(in);
                int id = in.readFourCC();

                if (id == RIFF_ID) {
                    parseFORM(props);
                } else if (id == LIST_ID) {
                    parseLIST(props);
                } else if (isLocalChunkID(id)) {
                    parseLocalChunk(chunk, id);
                } else {
                    ParseException pex = new ParseException("Invalid Chunk: \"" + id + "\" at offset:" + idscan);
                    chunk.setParserMessage(pex.getMessage());
                    throw pex;
                }

                in.align();
            }
        } catch (EOFException e) {
            e.printStackTrace();
            chunk.setParserMessage(
                  "Unexpected EOF after "
                  + NumberFormat.getInstance().format(getScan(in) - offset)
                  + " bytes");
        } finally {
            if (visitorWantsToEnterGroup) {
                visitor.leaveGroup(chunk);
            }
        }
    }

    /**
     * Parses a LIST group.
     * 
     * ListGroup ::= size GroupType { ChunkID LocalChunk [pad] | 'LIST ' ListGroup  [pad] }
     * 
     */
    private void parseLIST(HashMap<Integer, RIFFChunk> props)
          throws ParseException, AbortException, IOException {
        long size = in.readULONG();
        long scan = getScan(in);
        int type = in.readFourCC();
        if (DEBUG) {
            System.out.println("RIFFParser.parseLIST " + idToString(type));
        }

        if (!isGroupType(type)) {
            throw new ParseException("Invalid LIST Type: \"" + type + "\"");
        }

        RIFFChunk propGroup = (props == null) ? null : props.get(type);
        RIFFChunk chunk = new RIFFChunk(type, LIST_ID, size, scan, propGroup);

        boolean visitorWantsToEnterGroup = false;
        if (isGroupChunk(chunk) && (visitorWantsToEnterGroup = visitor.enteringGroup(chunk))) {
            visitor.enterGroup(chunk);
        }
        try {
            if (visitorWantsToEnterGroup) {
                long finish = scan + size;
                while (getScan(in) < finish) {
                    long idscan = getScan(in);
                    int id = in.readFourCC();
                    if (id == LIST_ID) {
                        parseLIST(props);
                    } else if (isLocalChunkID(id)) {
                        parseLocalChunk(chunk, id);
                    } else {
                        parseGarbage(chunk, id, finish - getScan(in), getScan(in));
                        ParseException pex = new ParseException("Invalid Chunk: \"" + id + "\" at offset:" + idscan);
                        chunk.setParserMessage(pex.getMessage());
                        //throw pex;
                    }

                    in.align();
                }
            } else {
                in.skipFully(size - 4);
                in.align();
            }
        } finally {
            if (visitorWantsToEnterGroup) {
                visitor.leaveGroup(chunk);
            }
        }
    }

    /**
     * Parses a local chunk.
     * 
     * LocalChunk  ::= size { DataChunk | PropertyChunk | CollectionChunk }
     * DataChunk = PropertyChunk = CollectionChunk ::= { byte }...*size
     * 
     */
    private void parseLocalChunk(RIFFChunk parent, int id)
          throws ParseException, AbortException, IOException {
        long size = in.readULONG();
        long scan = getScan(in);
        if (DEBUG) {
            System.out.println("RIFFParser.parseLocalChunk " + idToString(id));
        }
        RIFFChunk chunk = new RIFFChunk(parent == null ? 0 : parent.getType(), id, size, scan);

        if (isDataChunk(chunk)) {
            if (readData || (dataChunks != null && dataChunks.contains(chunk))) {
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
            in.skipFully((int) size);
            if (isStopChunks) {
                visitor.visitChunk(parent, chunk);
            }
        }
    }

    /**
     * This method is invoked when we encounter a parsing problem.
     * 
     * LocalChunk  ::= size { DataChunk | PropertyChunk | CollectionChunk }
     * DataChunk = PropertyChunk = CollectionChunk ::= { byte }...*size
     * 
     */
    private void parseGarbage(RIFFChunk parent, int id, long size, long scan)
          throws ParseException, AbortException, IOException {
        //long size = in.readULONG();
        //long scan = getScan(in);

        RIFFChunk chunk = new RIFFChunk(parent.getType(), id, size, scan);

        if (isDataChunk(chunk)) {
            byte[] data = new byte[(int) size];
            in.read(data, 0, (int) size);
            chunk.setData(data);
            visitor.visitChunk(parent, chunk);
        } else if (isPropertyChunk(chunk)) {
            byte[] data = new byte[(int) size];
            in.read(data, 0, (int) size);
            chunk.setData(data);
            parent.putPropertyChunk(chunk);
        } else if (isCollectionChunk(chunk)) {
            byte[] data = new byte[(int) size];
            in.read(data, 0, (int) size);
            chunk.setData(data);
            parent.addCollectionChunk(chunk);
        } else {
            in.skipFully((int) size);
            if (isStopChunk(chunk)) {
                visitor.visitChunk(parent, chunk);
            }
        }
    }

    /**
     * Checks whether the ID of the chunk has been declared as a data chunk.
     *
     * 
     * Pre condition Data chunks must have been declared before the
     * interpretation has been started. This method will always return true when
     * neither data chunks, property chunks nor collection chunks have been
     * declared,
     *
     * @param	chunk Chunk to be verified.
     * @return True when the parameter is a data chunk.
     */
    protected boolean isDataChunk(RIFFChunk chunk) {
        if (dataChunks == null) {
            if (collectionChunks == null && propertyChunks == null
                  && (stopChunkTypes == null || !stopChunkTypes.contains(chunk.getType()))) {
                return true;
            } else {
                return false;
            }
        } else {
            return dataChunks.contains(chunk);
        }
    }

    /**
     * Checks whether the ID of the chunk has been declared as a group chunk.
     *
     * 
     * Pre condition Group chunks must have been declared before the
     * interpretation has been started. (Otherwise the response is always true).
     *
     * @param	chunk Chunk to be verified.
     * @return True when the visitor is interested in this is a group chunk.
     */
    protected boolean isGroupChunk(RIFFChunk chunk) {
        if (groupChunks == null) {
            return true;
        } else {
            return groupChunks.contains(chunk);
        }
    }

    /**
     * Checks wether the ID of the chunk has been declared as a property chunk.
     *
     * 
     * Pre condition Property chunks must have been declared before the
     * interpretation has been started. This method will always return false
     * when neither data chunks, property chunks nor collection chunks have been
     * declared,
     *
     * @param chunk TODO
     * @return TODO
     */
    protected boolean isPropertyChunk(RIFFChunk chunk) {
        if (propertyChunks == null) {
            return false;
        } else {
            return propertyChunks.contains(chunk);
        }
    }

    /**
     * Checks wether the ID of the chunk has been declared as a collection
     * chunk.
     *
     * 
     * Pre condition Collection chunks must have been declared before the
     * interpretation has been started. This method will always return true when
     * neither data chunks, property chunks nor collection chunks have been
     * declared,
     *
     * @param	chunk Chunk to be verified.
     * @return True when the parameter is a collection chunk.
     */
    protected boolean isCollectionChunk(RIFFChunk chunk) {
        if (collectionChunks == null) {
            return false;
        } else {
            return collectionChunks.contains(chunk);
        }
    }

    /**
     * Declares a data chunk.
     *
     * 
     * Pre condition The chunk must not have already been declared as of a
     * different type. Declarations may not be done during interpretation of an
     * RIFFFileExpression.
     *
     * 
     * Post condition Data chunk declared
     *
     * @param	type Type of the chunk. Must be formulated as a TypeID conforming
     * to the method #isFormType.
     * @param	id ID of the chunk. Must be formulated as a ChunkID conforming to
     * the method #isLocalChunkID.
     */
    public void declareDataChunk(int type, int id) {
        RIFFChunk chunk = new RIFFChunk(type, id);
        if (dataChunks == null) {
            dataChunks = new HashSet<RIFFChunk>();
        }
        dataChunks.add(chunk);
    }

    /**
     * Declares a FORM group chunk.
     *
     * 
     * Pre condition The chunk must not have already been declared as of a
     * different type. Declarations may not be done during interpretation of an
     * RIFFFileExpression.
     *
     * 
     * Post condition Group chunk declared
     *
     * @param	type Type of the chunk. Must be formulated as a TypeID conforming
     * to the method #isFormType.
     * @param	id ID of the chunk. Must be formulated as a ChunkID conforming to
     * the method #isContentsType.
     */
    public void declareGroupChunk(int type, int id) {
        RIFFChunk chunk = new RIFFChunk(type, id);
        if (groupChunks == null) {
            groupChunks = new HashSet<RIFFChunk>();
        }
        groupChunks.add(chunk);
    }

    /**
     * Declares a property chunk.
     *
     * 
     * Pre condition The chunk must not have already been declared as of a
     * different type. Declarations may not be done during interpretation of an
     * RIFFFileExpression.
     *
     * 
     * Post condition Group chunk declared
     *
     *
     * @param	type Type of the chunk. Must be formulated as a TypeID conforming
     * to the method #isFormType.
     * @param	id ID of the chunk. Must be formulated as a ChunkID conforming to
     * the method #isLocalChunkID.
     */
    public void declarePropertyChunk(int type, int id) {
        RIFFChunk chunk = new RIFFChunk(type, id);
        if (propertyChunks == null) {
            propertyChunks = new HashSet<RIFFChunk>();
        }
        propertyChunks.add(chunk);
    }

    /**
     * Declares a collection chunk.
     *
     * 
     * Pre condition The chunk must not have already been declared as of a
     * different type. Declarations may not be done during interpretation of an
     * RIFFFileExpression.
     *
     * 
     * Post condition Collection chunk declared
     *
     * @param	type Type of the chunk. Must be formulated as a TypeID conforming
     * to the method #isFormType.
     * @param	id ID of the chunk. Must be formulated as a ChunkID conforming to
     * the method #isLocalChunkID.
     */
    public void declareCollectionChunk(int type, int id) {
        RIFFChunk chunk = new RIFFChunk(type, id);
        if (collectionChunks == null) {
            collectionChunks = new HashSet<RIFFChunk>();
        }
        collectionChunks.add(chunk);
    }

    /**
     * Declares a stop chunk.
     *
     * 
     * Pre condition The chunk must not have already been declared as of a
     * different type. Declarations may not be done during interpretation of an
     * RIFFFileExpression.
     *
     * 
     * Post condition Stop chunk declared
     *
     * @param	type Type of the chunk. Must be formulated as a TypeID conforming
     * to the method #isFormType.
     */
    public void declareStopChunkType(int type) {
        if (stopChunkTypes == null) {
            stopChunkTypes = new HashSet<Integer>();
        }
        stopChunkTypes.add(type);
    }

    /**
     * Whether the parse should stop at all chunks.
     * 
     * The parser does not read the data body of stop chunks.
     * 
     * By declaring stop chunks, and not declaring any data, group or property
     * chunks, the file structure of a RIFF file can be quickly scanned through.
     */
    public void declareStopChunks() {
        isStopChunks = true;
    }

    private boolean isStopChunk(RIFFChunk chunk) {
        return isStopChunks || stopChunkTypes != null && stopChunkTypes.contains(chunk.getType());
    }

    /* ---- Class methods ---- */
    /**
     * Checks wether the argument represents a valid RIFF GroupID.
     *
     * 
     * Validation  Group ID must be one of RIFF_ID, LIST_ID.
     * 
     *
     * @param	id Chunk ID to be checked.
     * @return	True when the chunk ID is a valid Group ID.
     */
    public static boolean isGroupID(int id) {
        return id == LIST_ID || id == RIFF_ID;
    }

    /**
     * Checks wether the argument represents a valid RIFF Group Type.
     *
     * 
     * Validation  Must be a valid ID. Must not be a group ID. Must not be a
     * NULL_ID. 
     *
     * @param	id Chunk ID to be checked.
     * @return	True when the chunk ID is a valid Group ID.
     */
    public static boolean isGroupType(int id) {
        return isID(id) && !isGroupID(id) && id != NULL_ID;
    }

    /**
     * Checks if the argument represents a valid RIFF ID.
     *
     * 
     * Validation Every byte of an ID must be in the range of 0x20..0x7e The id
     * may not have leading spaces (unless the id is a NULL_ID).
     *
     * @param	id Chunk ID to be checked.
     * @return	True when the ID is a valid IFF chunk ID.
     */
    public static boolean isID(int id) {
        int c0 = id >> 24;
        int c1 = (id >> 16) & 0xff;
        int c2 = (id >> 8) & 0xff;
        int c3 = id & 0xff;

        return id == NULL_NUL_ID
              || c0 >= 0x20 && c0 <= 0x7e
              && c1 >= 0x20 && c1 <= 0x7e
              && c2 >= 0x20 && c2 <= 0x7e
              && c3 >= 0x20 && c3 <= 0x7e;
    }

    /**
     * Returns whether the argument is a valid Local Chunk ID.
     *
     * 
     * Validation Must be valid ID. Local Chunk IDs may not collide with
     * GroupIDs. Must not be a NULL_ID.
     *
     * @param	id Chunk ID to be checked.
     * @return	True when the chunk ID is a Local Chunk ID.
     */
    public static boolean isLocalChunkID(int id) {
        if (isGroupID(id)) {
            return false;
        }
        return id != NULL_ID && isID(id);
    }
    private WeakHashMap<String, String> ids;

    /**
     * Convert an integer IFF identifier to String.
     *
     * @param	anInt ID to be converted.
     * @return	String representation of the ID.
     */
    public static String idToString(int anInt) {
        byte[] bytes = new byte[4];

        bytes[0] = (byte) (anInt >>> 24);
        bytes[1] = (byte) (anInt >>> 16);
        bytes[2] = (byte) (anInt >>> 8);
        bytes[3] = (byte) (anInt >>> 0);

        try {
            return new String(bytes, "ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(e.getMessage());
        }
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
