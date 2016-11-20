/* @(#)StructParser.java
 * Copyright © 2000-2013 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.binary;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import ru.sbtqa.monte.media.ParseException;
import ru.sbtqa.monte.media.io.ByteArrayImageInputStream;
import ru.sbtqa.monte.media.io.StreamPosTokenizer;
import ru.sbtqa.monte.media.math.ExtendedReal;

/**
 * Parses structured binary data using C-like data declarations.
 *
 * Syntax:
 * <pre><code>
 * Declarations ::= { MagicDeclaration | DescriptionDeclaration | EnumDeclaration | SetDeclaration | TypedefDeclaration } EOF
 * MagicDeclaration ::= "magic" identifier ("ushort" hexLiteral[".." hexLiteral] | stringLiteral) ";"
 * DescriptionDeclaration ::= "description" identifier stringLiteral "," stringLiteral ";"
 * EnumDeclaration ::= EnumSpecifier identifier ";"
 * SetDeclaration ::= SetSpecifier identifier ";"
 * TypedefDeclaration ::= "typedef" TypeSpecifier identifier ";"
 * EnumSpecifier ::= "enum" ( identifier | "{" identifier ["=" magicOrIntLiteral] {"," identifier ["=" intLiteral]} "}" )
 * SetSpecifier ::= "set" ( identifier | "{" identifier "=" intLiteral {"," identifier "=" intLiteral} "}" )
 * TypeSpecifier ::= ( StructSpecifier | (PrimitiveSpecifier [EnumSpecifier | SetSpecifier]) ) [ArrayList]
 * StructSpecifier ::= "struct (identifier | "{" MemberDeclaration {"," MemberDeclaration } "}" )
 * MemberDeclaration ::= TypeSpecifier identifier [ArrayList] ";"
 * PrimitiveSpecifier ::= "uint1" | "uint2" | "uint4" | "uint5" | "uint8" 
 *                         | "uint12" | "uint16" | "uint31LE" | "uint32"
 *                         | "int9" | "int16" | "int32"
 *                         | "ubyte" | "byte" | "short"
 *                         | "ushort" | "int" | "long" | "float" | "double"
 *                         | "extended"
 *                         | "char" | "charbyte" | "cstring" | "utf8" | "pstring"
 *                         | "pstring32"
 *                         | "utf16le" | "magic" | "mactimestamp" | "bcd2"
 *                         | "bcd4"
 *                         | "fixed16d16" | "fixed2d30" | "fixed8d8"
 *                         | "ataricolor"
 * ArrayList ::= "[" [ArraySize] "]" {"," identifier "[" [ArraySize] "]" }
 * ArraySize ::= () | intLiteral | identifier [("-"|"+"|"=="|"!=") intLiteral]
 * </code></pre>
 * @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Swityerland
 *
 * @version $Id: StructParser.java 364 2016-11-09 19:54:25Z werner $
 */
public class StructParser extends Object {

    protected static final long MAC_TIMESTAMP_EPOCH = new GregorianCalendar(1904, GregorianCalendar.JANUARY, 1).getTimeInMillis();
    private Declarations declarations;

    public StructParser() {
    }

    public StructParser(Reader r) throws IOException, ParseException {
        parse(r);
    }

    public String getName(String magic) {
        return getName((Object) magic);
    }

    public String getName(Object magic) {
        MagicDeclaration md = declarations.magics.get(magic);
        DescriptionDeclaration dd = (md == null) ? null : declarations.descriptions.get(md.identifier);
        return (dd == null) ? null : dd.name;
    }

    public String getIdentifierName(String magic) {
        MagicDeclaration md = declarations.magics.get(magic);
        DescriptionDeclaration dd = (md == null) ? null : declarations.descriptions.get(md.identifier);
        return (md == null) ? null : md.identifier;
    }

    public boolean isMagicDeclared(String magic) {
        return isMagicDeclared((Object) magic);
    }

    public boolean isMagicDeclared(Object magic) {
        MagicDeclaration md = declarations.magics.get(magic);
        return md != null;
    }

    public boolean isTypeDeclared(String magic) {
        return isTypeDeclared((Object) magic);
    }

    public boolean isTypeDeclared(Object magic) {
        TypedefDeclaration typedef = null;

        //DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
        MagicDeclaration magicdef = declarations.magics.get(magic);
        if (magicdef == null) {
            return false;
        }
        typedef = declarations.typedefs.get(magicdef.identifier);
        return typedef != null;
    }

    public String getDescription(String magic) {
        return getDescription((Object) magic);
    }

    public String getDescription(Object magic) {
        MagicDeclaration md = declarations.magics.get(magic);
        DescriptionDeclaration dd = (md == null) ? null : declarations.descriptions.get(md.identifier);
        return (dd == null) ? null : dd.description;
    }

    public StructTableModel readStruct(String magic, byte[] data)
            throws IOException {
        return declarations.readStruct(magic, new ByteArrayImageInputStream(data));
    }

    public StructTableModel readStruct(int magic, byte[] data)
            throws IOException {
        return declarations.readStruct(magic, new ByteArrayImageInputStream(data));
    }

    public StructTableModel readStruct(String magic, InputStream data)
            throws IOException {
        if (data instanceof ImageInputStream) {
            return declarations.readStruct(magic, (ImageInputStream) data);
        } else {
            return declarations.readStruct(magic, new MemoryCacheImageInputStream(data));
        }
    }

    public StructTableModel readStruct(int magic, InputStream data)
            throws IOException {
        if (data instanceof ImageInputStream) {
            return declarations.readStruct(magic, (ImageInputStream) data);
        } else {
            return declarations.readStruct(magic, new MemoryCacheImageInputStream(data));
        }
    }

    /**
     * Consumes the number of bytes needed by the specified struct from the
     * input stream, and returns them as a byte array.
     */
    private static String errorMsg(String text, StreamPosTokenizer scanner) {
        StringBuilder b = new StringBuilder();
        b.append("line ");
        b.append(Integer.toString(scanner.lineno()));
        /*        b.append(' ');
        b.append(Integer.toString(scanner.getStartPosition()));
        b.append("..");
        b.append(Integer.toString(scanner.getEndPosition()));*/
        b.append(": ");
        b.append(text);
        b.append(" instead of ");
        switch (scanner.ttype) {
            case StreamPosTokenizer.TT_WORD:
                b.append('\'');
                b.append(scanner.sval);
                b.append('\'');
                break;
            case StreamPosTokenizer.TT_EOF:
                b.append("EOF");
                break;
            case StreamPosTokenizer.TT_EOL:
                b.append("EOL");
                break;
            case StreamPosTokenizer.TT_NUMBER:
                b.append(Double.toString(scanner.nval));
                break;
            case '"':
                b.append('"');
                b.append(scanner.sval);
                b.append('"');
                break;
            default:
                b.append('\'');
                b.append((char) scanner.ttype);
                b.append('\'');
                b.append("(0x");
                b.append(Integer.toHexString(scanner.ttype));
                b.append(')');
                break;
        }
        return b.toString();
    }

    protected void parse(Reader r)
            throws IOException, ParseException {
        StreamPosTokenizer scanner = new StreamPosTokenizer(r);
        scanner.resetSyntax();
        scanner.wordChars('a', 'z');
        scanner.wordChars('A', 'Z');
        scanner.wordChars(128 + 32, 255);
        scanner.wordChars('_', '_');
        scanner.whitespaceChars(0, ' ');
        //scanner.commentChar('/');
        scanner.quoteChar('"');
        scanner.quoteChar('\'');
        scanner.parseNumbers();
        scanner.parseHexNumbers();
        scanner.slashSlashComments(true);
        scanner.slashStarComments(true);
        scanner.ordinaryChar(';');
        scanner.ordinaryChar('[');
        scanner.ordinaryChar(']');
        scanner.ordinaryChar('{');
        scanner.ordinaryChar('}');
        scanner.ordinaryChar('+');
        scanner.ordinaryChar('-');

        declarations = new Declarations(scanner);
    }

    /**
     * Declarations expression.
     *
     * <pre><code>
     * Declarations ::= { MagicDeclaration | DescriptionDeclaration | EnumDeclaration | SetDeclaration | TypedefDeclaration }
     * </code></pre>
     */
    protected static class Declarations {

        /** Magic key can be either a String or an Integer. */
        public HashMap<Object,MagicDeclaration> magics = new HashMap<Object,MagicDeclaration>();
        public HashMap<String,DescriptionDeclaration> descriptions = new HashMap<String,DescriptionDeclaration>();
        public HashMap<String,EnumDeclaration> enums = new HashMap<String,EnumDeclaration>();
        public HashMap<String,SetDeclaration> sets = new HashMap<String,SetDeclaration>();
        public HashMap<String,TypedefDeclaration> typedefs = new HashMap<String,TypedefDeclaration>();

        public Declarations(StreamPosTokenizer scanner)
                throws IOException, ParseException {
            while (scanner.nextToken() != StreamPosTokenizer.TT_EOF) {
                if (scanner.ttype == StreamPosTokenizer.TT_WORD) {
                    if (scanner.sval.equals("magic")) {
                        scanner.pushBack();
                        MagicDeclaration md = new MagicDeclaration(scanner);
                        if (md.magic != null) {
                            magics.put(md.magic, md);
                        } else {
                            for (int i = md.ushortMagicFrom; i <= md.ushortMagicTo; i++) {
                                magics.put(i, md);
                            }
                        }
                    } else if (scanner.sval.equals("description")) {
                        scanner.pushBack();
                        DescriptionDeclaration dd = new DescriptionDeclaration(scanner);
                        descriptions.put(dd.identifier, dd);
                    } else if (scanner.sval.equals("enum")) {
                        scanner.pushBack();
                        EnumDeclaration ed = new EnumDeclaration(scanner);
                        enums.put(ed.identifier, ed);
                    } else if (scanner.sval.equals("set")) {
                        scanner.pushBack();
                        SetDeclaration sd = new SetDeclaration(scanner);
                        sets.put(sd.identifier, sd);
                    } else if (scanner.sval.equals("typedef")) {
                        scanner.pushBack();
                        TypedefDeclaration td = new TypedefDeclaration(scanner);
                        typedefs.put(td.identifier, td);
                    } else {
                        throw new ParseException(errorMsg("Declarations: Expected 'magic', 'description', 'enum', 'set' or 'typedef' ", scanner));
                    }
                }
            }
        }

        private StructTableModel readStruct(Object magic, ImageInputStream in)
                throws IOException {
            ArrayList<StructTableModel.Value> result = new ArrayList<StructTableModel.Value>();
            TypedefDeclaration typedef = null;

            //DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
            MagicDeclaration magicdef = magics.get(magic);
            if (magicdef == null) {
                //System.out.println("StructParser.StructTableModel.readStruct() unknow magic:"+magic);
                //new Exception().printStackTrace();
                if (magic instanceof Integer) {
                    throw new IOException("unknown magic:" + Integer.toHexString((Integer) magic));
                } else {
                    throw new IOException("unknown magic:" + magic);
                }
            }
            typedef = typedefs.get(magicdef.identifier);
            if (typedef == null) {
                //System.out.println("StructParser.StructTableModel.readStruct() typedef not found for magic:"+magic+" magicdef.identifier:"+magicdef.identifier);
                throw new IOException("unknown type:" + magicdef.identifier);
            }
            try {
                Object obj = typedef.read(in, typedef.identifier, this, result);
                if (obj != null) {
                    StructTableModel.Value value = new StructTableModel.Value();
                    value.declaration = typedef.identifier;
                    value.value = obj;
                    result.add(value);
                }
            } catch (EOFException e) {
            }
            return new StructTableModel(typedef, result);
        }
    }

    /**
     * MagicDeclaration expression.
     *
     * <pre><code>
     * MagicDeclaration ::= "magic" identifier ("ushort" hexLiteral[".." hexLiteral] | stringLiteral) ";"
     * </code></pre>
     */
    protected static class MagicDeclaration {

        public String identifier;
        /** If magic is null, then ushortMagicFrom/ushortMagicTo is used. */
        public String magic;
        public int ushortMagicFrom;
        public int ushortMagicTo;

        public MagicDeclaration(StreamPosTokenizer scanner)
                throws IOException, ParseException {
            if (scanner.nextToken() != StreamPosTokenizer.TT_WORD || !scanner.sval.equals("magic")) {
                throw new ParseException(errorMsg("MagicDeclaration: 'magic' expected", scanner));
            }
            if (scanner.nextToken() != StreamPosTokenizer.TT_WORD) {
                throw new ParseException(errorMsg("MagicDeclaration: identifier expected", scanner));
            }
            identifier = scanner.sval;

            if (scanner.nextToken() == StreamPosTokenizer.TT_WORD
                    && scanner.sval.equals("ushort")) {
                if (scanner.nextToken() != StreamPosTokenizer.TT_NUMBER) {
                    throw new ParseException(errorMsg("MagicDeclaration: ushort literal expected", scanner));
                }
                ushortMagicFrom = (int) scanner.nval;
                if (scanner.nextToken() == '.') {
                    if (scanner.nextToken() != '.') {
                        throw new ParseException(errorMsg("MagicDeclaration: interval literal expected", scanner));
                    }
                    if (scanner.nextToken() != StreamPosTokenizer.TT_NUMBER) {
                        throw new ParseException(errorMsg("MagicDeclaration: end interval literal expected", scanner));
                    }
                    ushortMagicTo = (int) scanner.nval;
                } else {
                    ushortMagicTo = ushortMagicFrom;
                    scanner.pushBack();
                }
            } else {
                if (scanner.ttype != '"') {
                    throw new ParseException(errorMsg("MagicDeclaration: string literal expected", scanner));
                }
                magic = scanner.sval;
            }
            if (scanner.nextToken() != ';') {
                throw new ParseException(errorMsg("MagicDeclaration: ';' expected", scanner));
            }
        }
    }
    /*
    private static int parseHexLiteral(StreamPosTokenizer scanner)
    throws IOException, ParseException {
    scanner.
    if (scanner.nextToken() != StreamPosTokenizer.TT_NUMBER || scanner.nval != 0) {
    throw new ParseException(errorMsg("MagicDeclaration: hex literal expected",scanner));
    }
    System.out.println("StructParser.MagicDeclaration nval::"+scanner.nval);
    System.out.println("StructParser.MagicDeclaration next TOken:"+scanner.nextToken());
    
    }*/

    /**
     * DescriptionDeclaration expression.
     *
     * <pre><code>
     * DescriptionDeclaration ::= "description" identifier stringLiteral "," stringLiteral ";"
     * </code></pre>
     */
    protected static class DescriptionDeclaration {

        public String identifier;
        public String name;
        public String description;

        public DescriptionDeclaration(StreamPosTokenizer scanner)
                throws IOException, ParseException {
            if (scanner.nextToken() != StreamPosTokenizer.TT_WORD || !scanner.sval.equals("description")) {
                throw new ParseException(errorMsg("DescriptionDeclaration: 'magic' expected", scanner));
            }
            if (scanner.nextToken() != StreamPosTokenizer.TT_WORD) {
                throw new ParseException(errorMsg("DescriptionDeclaration: identifier expected", scanner));
            }
            identifier = scanner.sval;
            if (scanner.nextToken() != '"') {
                throw new ParseException(errorMsg("DescriptionDeclaration: string literal (title) expected", scanner));
            }
            name = scanner.sval;
            if (scanner.nextToken() != ',') {
                throw new ParseException(errorMsg("DescriptionDeclaration: ',' expected", scanner));
            }
            if (scanner.nextToken() != '"') {
                throw new ParseException(errorMsg("DescriptionDeclaration: string literal (description) expected", scanner));
            }
            description = scanner.sval;
            while (scanner.nextToken() == '+') {
                if (scanner.nextToken() != '"') {
                    throw new ParseException(errorMsg("DescriptionDeclaration: string literal (description) after '+' expected", scanner));
                }
                description += scanner.sval;
            }
            scanner.pushBack();
            if (scanner.nextToken() != ';') {
                throw new ParseException(errorMsg("DescriptionDeclaration: ';' expected", scanner));
            }
        }
    }

    /**
     * EnumDeclaration expression.
     *
     * <pre><code>
     * EnumDeclaration ::= EnumSpecifier identifier ";"
     * </code></pre>
     */
    protected static class EnumDeclaration {

        public EnumSpecifier enumSpecifier;
        public String identifier;

        public EnumDeclaration(StreamPosTokenizer scanner)
                throws IOException, ParseException {
            enumSpecifier = new EnumSpecifier(scanner);

            if (scanner.nextToken() != StreamPosTokenizer.TT_WORD) {
                throw new ParseException(errorMsg("EnumDeclaration: identifier expected", scanner));
            }
            identifier = scanner.sval;

            if (scanner.nextToken() != ';') {
                throw new ParseException(errorMsg("EnumDeclaration: ';' expected", scanner));
            }
        }
    }

    /**
     * SetDeclaration expression.
     *
     * <pre><code>
     * SetDeclaration ::= SetSpecifier identifier ";"
     * </code></pre>
     */
    protected static class SetDeclaration {

        public SetSpecifier setSpecifier;
        public String identifier;

        public SetDeclaration(StreamPosTokenizer scanner)
                throws IOException, ParseException {
            setSpecifier = new SetSpecifier(scanner);

            if (scanner.nextToken() != StreamPosTokenizer.TT_WORD) {
                throw new ParseException(errorMsg("SetDeclaration: identifier expected", scanner));
            }
            identifier = scanner.sval;

            if (scanner.nextToken() != ';') {
                throw new ParseException(errorMsg("SetDeclaration: ';' expected", scanner));
            }
        }
    }

    /**
     * TypedefDeclaration expression.
     *
     * <pre><code>
     * TypedefDeclaration ::= "typedef" TypeSpecifier identifier ";"
     * </code></pre>
     */
    protected static class TypedefDeclaration {

        public TypeSpecifier typeSpecifier;
        public String identifier;

        public TypedefDeclaration(StreamPosTokenizer scanner)
                throws IOException, ParseException {
            if (scanner.nextToken() != StreamPosTokenizer.TT_WORD || !scanner.sval.equals("typedef")) {
                throw new ParseException(errorMsg("TypedefDeclaration: 'typedef' expected", scanner));
            }

            typeSpecifier = new TypeSpecifier(scanner);

            if (scanner.nextToken() != StreamPosTokenizer.TT_WORD) {
                throw new ParseException(errorMsg("TypedefDeclaration: identifier expected", scanner));
            }
            identifier = scanner.sval;

            if (scanner.nextToken() != ';') {
                throw new ParseException(errorMsg("TypedefDeclaration: ';' expected", scanner));
            }
            //System.out.println("typedef "+typeSpecifier+" "+identifier+";");
        }

        private int getResolvedType(Declarations declarations) throws IOException {
            return typeSpecifier.getResolvedPrimitiveType(declarations);
        }

        private Object read(ImageInputStream in, String parentIdentifier, Declarations declarations, ArrayList<StructTableModel.Value> result)
                throws IOException {
            return typeSpecifier.read(in, parentIdentifier, declarations, identifier, result);
        }
    }

    /**
     * EnumSpecifier expression.
     *
     * <pre><code>
     * EnumSpecifier ::= "enum" ( identifier | "{" identifier ["=" intLiteral] {"," identifier ["=" intLiteral]} "}" )
     * </code></pre>
     */
    protected static class EnumSpecifier {

        public HashMap<Integer,String> members;
        public String identifier;
        public boolean isMagicEnum;

        public EnumSpecifier(StreamPosTokenizer scanner)
                throws IOException, ParseException {
            if (scanner.nextToken() != StreamPosTokenizer.TT_WORD || !scanner.sval.equals("enum")) {
                throw new ParseException(errorMsg("EnumSpecifier: 'enum' expected", scanner));
            }

            if (scanner.nextToken() == StreamPosTokenizer.TT_WORD) {
                identifier = scanner.sval;
            } else if (scanner.ttype == '{') {
                String name;
                int value = -1;
                members = new HashMap<Integer,String>();
                do {
                    if (scanner.nextToken() == '}') {
                        break;
                    }
                    if (scanner.ttype != StreamPosTokenizer.TT_WORD
                            && scanner.ttype != '"') {
                        throw new ParseException(errorMsg("EnumSpecifier: enumeration name expected", scanner));
                    }
                    name = scanner.sval;

                    if (scanner.nextToken() == '=') {
                        MagicOrIntLiteral literal = new MagicOrIntLiteral(scanner);
                        value = literal.intValue();
                        isMagicEnum = literal.isMagic();
                    } else {
                        value++;
                        scanner.pushBack();
                    }

                    members.put(new Integer(value), name);
                } while (scanner.nextToken() == ',');
                if (scanner.ttype != '}') {
                    throw new ParseException(errorMsg("EnumSpecifier: '}' expected", scanner));
                }
            } else {
                throw new ParseException(errorMsg("EnumSpecifier: identifier or '{' expected", scanner));
            }
            identifier = scanner.sval;
        }

        public String toEnumString(int value, Declarations declarations) {
            if (identifier != null) {
                EnumDeclaration enumDecl = declarations.enums.get(identifier);
                if (enumDecl == null) {
                    throw new InternalError("Enum Declaration missing for " + identifier);
                }
                return enumDecl.enumSpecifier.toEnumString(value, declarations);
            } else {
                StringBuilder buf = new StringBuilder();
                Integer intValue = new Integer(value);
                if (isMagicEnum) {
                    buf.append('"');
                    buf.append(MagicOrIntLiteral.toMagic(value));
                    buf.append('"');
                } else {
                    buf.append(Integer.toString(value));
                    /*
                    buf.append("0x");
                    buf.append(Integer.toHexString(value));*/
                }
                buf.append(" {");
                if (members.get(intValue) != null) {
                    buf.append(members.get(intValue).toString());
                }
                buf.append('}');
                return buf.toString();
            }
        }

        public String toEnumString(String value, Declarations declarations) {
            if (identifier != null) {
                EnumDeclaration enumDecl = declarations.enums.get(identifier);
                if (enumDecl == null) {
                    throw new InternalError("Enum Declaration missing for " + identifier);
                }
                return enumDecl.enumSpecifier.toEnumString(value, declarations);
            } else {
                if (value == null || value.length() != 4 || !isMagicEnum) {
                    return value;
                } else {
                    return toEnumString(MagicOrIntLiteral.toInt(value), declarations);
                }
            }
        }
    }

    /**
     * SetSpecifier expression.
     *
     * <pre><code>
     * SetSpecifier ::= "set" ( identifier | "{" identifier "=" intLiteral {"," identifier "=" intLiteral} "}" )
     * </code></pre>
     */
    protected static class SetSpecifier {

        public HashMap<Integer,String> members;
        public String identifier;

        public SetSpecifier(StreamPosTokenizer scanner)
                throws IOException, ParseException {
            if (scanner.nextToken() != StreamPosTokenizer.TT_WORD || !scanner.sval.equals("set")) {
                throw new ParseException(errorMsg("SetSpecifier: 'set' expected", scanner));
            }

            if (scanner.nextToken() == StreamPosTokenizer.TT_WORD) {
                identifier = scanner.sval;
            } else if (scanner.ttype == '{') {
                String name;
                int value = 0;
                members = new HashMap<Integer,String>();
                do {

                    if (scanner.nextToken() != StreamPosTokenizer.TT_WORD && scanner.ttype!='"') {
                        throw new ParseException(errorMsg("SetSpecifier: set name expected", scanner));
                    }
                    name = scanner.sval;

                    if (scanner.nextToken() == '=') {
                        value = (new IntLiteral(scanner)).intValue();
                    } else {
                        value = (value == 0) ? 1 : value << 1;
                        scanner.pushBack();
                    }
                    members.put(new Integer(value), name);
                } while (scanner.nextToken() == ',');
                if (scanner.ttype != '}') {
                    throw new ParseException(errorMsg("SetSpecifier: '}' expected", scanner));
                }
            } else {
                throw new ParseException(errorMsg("SetSpecifier: identifier or '{' expected", scanner));
            }
            identifier = scanner.sval;
        }

        public String toSetString(int value, Declarations declarations) {
            if (identifier != null) {
                SetDeclaration setDecl = declarations.sets.get(identifier);
                if (setDecl == null) {
                    throw new InternalError("Set Declaration missing for " + identifier);
                }
                return setDecl.setSpecifier.toSetString(value, declarations);
            } else {
                StringBuilder buf = new StringBuilder();
                buf.append("0x");
                buf.append(Integer.toHexString(value));
                buf.append(" {");
                boolean isFirst = true;
                for (Map.Entry<Integer,String> entry:members.entrySet()) {
                    int intKey = entry.getKey();
                    if ((intKey == 0 && value == 0) || ((intKey & value) == intKey)) {
                        if (isFirst) {
                            isFirst = false;
                        } else {
                            buf.append(", ");
                        }
                        buf.append(entry.getValue());
                    }
                }
                buf.append('}');
                return buf.toString();
            }
        }
    }

    /**
     * TypeSpecifier expression.
     *
     * <pre><code>
     * TypeSpecifier ::= ( StructSpecifier | (PrimitiveSpecifier [EnumSpecifier | SetSpecifier]) ) [ArrayList]
     * ArrayList ::= "[" [ArraySize] "]" {"," identifier "[" [ArraySize] "]" }
     * </code></pre>
     */
    protected static class TypeSpecifier {

        public StructSpecifier structSpecifier;
        public PrimitiveSpecifier primitiveSpecifier;
        public EnumSpecifier enumSpecifier;
        public SetSpecifier setSpecifier;
        public ArrayList<ArraySize> arrayList;

        public TypeSpecifier(StreamPosTokenizer scanner)
                throws IOException, ParseException {
            if (scanner.nextToken() == StreamPosTokenizer.TT_WORD && scanner.sval.equals("struct")) {
                scanner.pushBack();
                structSpecifier = new StructSpecifier(scanner);
            } else {
                scanner.pushBack();
                primitiveSpecifier = new PrimitiveSpecifier(scanner);

                scanner.nextToken();
                if (scanner.ttype == StreamPosTokenizer.TT_WORD && scanner.sval.equals("enum")) {
                    scanner.pushBack();
                    enumSpecifier = new EnumSpecifier(scanner);
                } else if (scanner.ttype == StreamPosTokenizer.TT_WORD && scanner.sval.equals("set")) {
                    scanner.pushBack();
                    setSpecifier = new SetSpecifier(scanner);
                } else {
                    scanner.pushBack();
                }
            }

            // ArrayList Begin
            if (scanner.nextToken() == '[') {
                arrayList = new ArrayList<ArraySize>();
                do {
                    arrayList.add(new ArraySize(scanner));
                    if (scanner.nextToken() != ']') {
                        throw new ParseException(errorMsg("MemberDeclaration: ']' expected", scanner));
                    }
                } while (scanner.nextToken() == '[');
                scanner.pushBack();
            } else {
                scanner.pushBack();
            }
            // ArrayList End
        }

        private int getResolvedPrimitiveType(Declarations declarations) throws IOException {
            if (structSpecifier != null) {
                return PrimitiveSpecifier.NON_PRIMITIVE;
            } else if (primitiveSpecifier != null) {
                return primitiveSpecifier.getResolvedPrimitveType(declarations);
            } else {
                return PrimitiveSpecifier.NON_PRIMITIVE;
            }
        }

        private Object read(ImageInputStream in, String parentIdentifier, Declarations declarations, String identifier, ArrayList<StructTableModel.Value> result)
                throws IOException {
            if (structSpecifier != null) {
                if (arrayList != null) {
                    // ArrayList Begin
                    for (ArraySize arraySize:arrayList) {
                        int size = arraySize.getArraySize(declarations, result);
                        if (size == ArraySize.REMAINDER) {
                            try {
                                for (int i = 0;; i++) {
                                    int loc = result.size();
                                    structSpecifier.read(in, parentIdentifier, declarations, result);

                                    for (int j = loc; j < result.size(); j++) {
                                        StructTableModel.Value arrayValue = result.get(j);
                                        arrayValue.index = (arrayValue.index == null) ? "[" + i + "]" : "[" + i + "]" + arrayValue.index;
                                    }
                                }
                            } catch (EOFException e) {
                            }
                        } else {
                            for (int i = 0; i < size; i++) {
                                int loc = result.size();
                                structSpecifier.read(in, parentIdentifier, declarations, result);

                                for (int j = loc; j < result.size(); j++) {
                                    StructTableModel.Value arrayValue = result.get(j);
                                    arrayValue.index = (arrayValue.index == null) ? "[" + i + "]" : "[" + i + "]" + arrayValue.index;
                                }
                            }
                        }
                    }
                    return null;
                } else {
                    structSpecifier.read(in, parentIdentifier, declarations, result);
                    return null;
                }
            } else if (primitiveSpecifier.getResolvedPrimitveType(declarations) == PrimitiveSpecifier.CHAR) {
                if (arrayList != null) {
                    StringBuilder buf = new StringBuilder();
                    buf.append('\"');
                    boolean hasValue = false;

                    // ArrayList Begin
                    for (ArraySize arraySize:arrayList) {
                        int size = arraySize.getArraySize(declarations, result);
                        if (size == ArraySize.REMAINDER) {
                            try {
                                for (int i = 0;; i++) {
                                    //if (i > 0) {
                                    //  buf.append(", ");
                                    //}
                                    Object value = readValue(in, parentIdentifier + "[" + i + "]", declarations, result);
                                    if (value != null) {
                                        hasValue = true;
                                        if (value.toString().equals("0x20")) {
                                            buf.append(' ');
                                        } else {
                                            buf.append(value.toString());
                                        }
                                    }
                                }
                            } catch (EOFException e) {
                            }
                        } else {
                            for (int i = 0; i < size; i++) {
                                //if (i > 0) {
                                //    buf.append(", ");
                                //}
                                Object value = readValue(in, parentIdentifier + "[" + i + "]", declarations, result);
                                if (value != null) {
                                    hasValue = true;
                                    if (value.toString().equals("0x20")) {
                                        buf.append(' ');
                                    } else {
                                        buf.append(value.toString());
                                    }
                                }
                            }
                        }
                    }
                    // ArrayList End
                    if (hasValue) {
                        buf.append('\"');
                        return buf.toString();
                    } else {
                        return null;
                    }
                } else {
                    return readValue(in, parentIdentifier, declarations, result);
                }
            } else if (primitiveSpecifier.getResolvedPrimitveType(declarations) == PrimitiveSpecifier.UBYTE) {
                if (arrayList != null) {
                    StringBuilder buf = new StringBuilder();
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    buf.append("0x");
                    boolean hasValue = false;

                    // ArrayList Begin
                    for (ArraySize arraySize:arrayList) {
                        int size = arraySize.getArraySize(declarations, result);
                        if (size == ArraySize.REMAINDER) {
                            try {
                                for (int i = 0;; i++) {
                                    //if (i > 0) {
                                    //  buf.append(", ");
                                    //}
                                    Integer value = (Integer) readValue(in, parentIdentifier + "[" + i + "]", declarations, result);
                                    if (value != null) {
                                        hasValue = true;
                                        String str = Integer.toHexString(value & 0xff);
                                        if (str.length() == 1) {
                                            buf.append('0');
                                        }
                                        buf.append(str);
                                        bout.write(value);
                                    }
                                }
                            } catch (EOFException e) {
                            }
                        } else {
                            for (int i = 0; i < size; i++) {
                                //if (i > 0) {
                                //    buf.append(", ");
                                //}
                                Integer value = (Integer) readValue(in, parentIdentifier + "[" + i + "]", declarations, result);
                                if (value != null) {
                                    hasValue = true;
                                    String str = Integer.toHexString(value & 0xff);
                                    if (str.length() == 1) {
                                        buf.append('0');
                                    }
                                    buf.append(str);
                                    bout.write(value);
                                }
                            }
                        }
                    }
                    // ArrayList End
                    if (hasValue) {
                        if (true) {
                            return bout.toByteArray();
                        }
                        //buf.append('}');
                        return buf.toString();
                    } else {
                        return null;
                    }
                } else {
                    return readValue(in, parentIdentifier, declarations, result);
                }
            } else {
                if (arrayList != null) {
                    StringBuilder buf = new StringBuilder();
                    buf.append('{');
                    boolean hasValue = false;

                    // ArrayList Begin
                    for (ArraySize arraySize:arrayList) {
                        int size = arraySize.getArraySize(declarations, result);
                        if (size == ArraySize.REMAINDER) {
                            try {
                                for (int i = 0;; i++) {
                                    if (i > 0) {
                                        buf.append(", ");
                                    }
                                    Object value = readValue(in, parentIdentifier + "[" + i + "]", declarations, result);
                                    if (value != null) {
                                        hasValue = true;
                                        buf.append(value.toString());
                                    }
                                }
                            } catch (EOFException e) {
                            }
                        } else {
                            for (int i = 0; i < size; i++) {
                                if (i > 0) {
                                    buf.append(", ");
                                }
                                Object value;
                                if (arraySize.type == ArraySize.EQUAL || arraySize.type == ArraySize.NOT_EQUAL) {
                                    value = readValue(in, parentIdentifier, declarations, result);
                                } else {
                                    value = readValue(in, parentIdentifier + "[" + i + "]", declarations, result);
                                }
                                if (value != null) {
                                    hasValue = true;
                                    buf.append(value.toString());
                                }
                            }
                        }
                    }
                    // ArrayList End
                    if (hasValue) {
                        buf.append('}');
                        return buf.toString();
                    } else {
                        return null;
                    }
                } else {
                    return readValue(in, parentIdentifier, declarations, result);
                }
            }
        }

        private Object readValue(ImageInputStream in, String parentIdentifier, Declarations declarations, ArrayList<StructTableModel.Value> result)
                throws IOException {
            Object value = primitiveSpecifier.read(in, parentIdentifier, declarations, result);
            if (value != null) {
                if (value instanceof Number) {
                    int intValue = ((Number) value).intValue();
                    if (enumSpecifier != null) {
                        value = enumSpecifier.toEnumString(intValue, declarations);
                    } else if (setSpecifier != null) {
                        value = setSpecifier.toSetString(intValue, declarations);
                    }
                } else if (value instanceof String) {
                    if (enumSpecifier != null) {
                        value = enumSpecifier.toEnumString((String) value, declarations);
                    }
                }
            }
            return value;
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append("TypeSpecifier ");
            buf.append(structSpecifier);
            buf.append(primitiveSpecifier);
            buf.append(enumSpecifier);
            buf.append(setSpecifier);
            buf.append(arrayList);
            return buf.toString();
        }
    }

    /**
     * StructSpecifier expression.
     *
     * <pre><code>
     * StructSpecifier ::= "struct (identifier | "{" MemberDeclaration { MemberDeclaration } "}" )
     * </code></pre>
     */
    protected static class StructSpecifier {

        public String identifier;
        public ArrayList<MemberDeclaration> members;

        public StructSpecifier(StreamPosTokenizer scanner)
                throws IOException, ParseException {
            if (scanner.nextToken() != StreamPosTokenizer.TT_WORD || !scanner.sval.equals("struct")) {
                throw new ParseException(errorMsg("StructSpecifier: 'struct' expected", scanner));
            }

            scanner.nextToken();
            if (scanner.ttype == StreamPosTokenizer.TT_WORD) {
                identifier = scanner.sval;
            } else if (scanner.ttype == '{') {
                members = new ArrayList<MemberDeclaration>();
                while (scanner.nextToken() != '}') {
                    scanner.pushBack();

                    members.add(new MemberDeclaration(scanner));
                }
            } else {
                throw new ParseException(errorMsg("StructSpecifier: identifier or '{' expected", scanner));
            }
        }

        private void read(ImageInputStream in, String parentIdentifier, Declarations declarations, ArrayList<StructTableModel.Value> result)
                throws IOException {
            //try {
            for (MemberDeclaration aMember:members) {
               aMember.read(in, parentIdentifier, declarations, result);
            }
            /*
            } catch (IOException e) {
            System.out.println("StructParser.StructSpecifier.read(...) IOException @ "+identifier);
            e.printStackTrace();
            throw e;
            }*/
        }
    }

    /**
     * MemberDeclaration expression.
     *
     * <pre><code>
     * MemberDeclaration ::= TypeSpecifier identifier [ArrayList] ";"
     * ArrayList ::= "[" [ArraySize] "]" {"," identifier "[" [ArraySize] "]" }
     * </code></pre>
     */
    protected static class MemberDeclaration {

        public TypeSpecifier typeSpecifier;
        public String identifier;
        public ArrayList<ArraySize> arrayList;

        public MemberDeclaration(StreamPosTokenizer scanner)
                throws IOException, ParseException {
            typeSpecifier = new TypeSpecifier(scanner);

            if (scanner.nextToken() != StreamPosTokenizer.TT_WORD) {
                throw new ParseException(errorMsg("MemberDeclaration: identifier expected", scanner));
            }
            identifier = scanner.sval;

            // ArrayList Begin
            if (scanner.nextToken() == '[') {
                arrayList = new ArrayList<ArraySize>();
                do {
                    arrayList.add(new ArraySize(scanner));
                    if (scanner.nextToken() != ']') {
                        throw new ParseException(errorMsg("MemberDeclaration: ']' expected", scanner));
                    }
                } while (scanner.nextToken() == '[');
                scanner.pushBack();
            } else {
                scanner.pushBack();
            }
            // ArrayList End

            if (scanner.nextToken() == ';') {
            } else if (scanner.ttype == '}') {
                scanner.pushBack();
            } else {
                throw new ParseException(errorMsg("MemberDeclaration: ';' expected", scanner));
            }
        }

        private void read(ImageInputStream in, String parentIdentifier, Declarations declarations, ArrayList<StructTableModel.Value> result)
                throws IOException {
            if (arrayList != null) {
                // ArrayList Begin
                for (ArraySize arraySize :arrayList) {
                    int size = arraySize.getArraySize(declarations, result);
                    if (size == ArraySize.REMAINDER) {
                        try {
                            for (int i = 0;; i++) {
                                int loc = result.size();
                                Object obj = typeSpecifier.read(in, parentIdentifier + "." + identifier + "[" + i + "]", declarations, identifier, result);
                                if (obj != null) {
                                    StructTableModel.Value value = new StructTableModel.Value();
                                    value.qualifiedIdentifier = parentIdentifier + "." + identifier;
                                    value.declaration = identifier;
                                    value.value = obj;
                                    result.add(value);
                                }
                            }
                        } catch (EOFException e) {
                        }
                    } else {
                        for (int i = 0; i < size; i++) {
                            int loc = result.size();
                            Object obj = typeSpecifier.read(in, parentIdentifier + "." + identifier + "[" + i + "]", declarations, identifier, result);
                            if (obj != null) {
                                StructTableModel.Value value = new StructTableModel.Value();
                                value.qualifiedIdentifier = parentIdentifier + "." + identifier+ "[" + i + "]";
                                value.declaration = identifier;
                                value.value = obj;
                                result.add(value);
                            }
                        }
                    }
                }
                // ArrayList End
            } else {
                Object obj = typeSpecifier.read(in, parentIdentifier + "." + identifier, declarations, identifier, result);
                if (obj != null) {
                    StructTableModel.Value value = new StructTableModel.Value();
                    value.qualifiedIdentifier = parentIdentifier + "." + identifier;
                    value.declaration = identifier;
                    value.value = obj;
                    result.add(value);
                }
            }

        }
    }

    /**
     * PrimitiveSpecifier expression.
     *
     * <pre><code>
     * PrimitiveSpecifier ::= "uint" n | "ubyte" | "byte" | "short" | "ushort" |
     *                         | "int" | "long" | "float" | "double" | "extended"
     *                         | "char" | "charbyte" | "cstring" | "utf8" | "pstring"
     *                         | "pstring32"
     *                         | "utf16le" |"magic" | "mactimestamp"
     *                         | "bcd2" | "bcd4"
     * </code></pre>
     */
    protected static class PrimitiveSpecifier {

        private final static int NON_PRIMITIVE = -1;
        private final static char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        /* 8 bits signed. */
        public final static int BYTE = 0;
        /* 8 bits unsigned. */
        public final static int UBYTE = 1;
        /* 16 bits signed big endian. */
        public final static int SHORT = 2;
        /* 16 bits unsigned big endian. */
        public final static int USHORT = 3;
        /* 32 bits signed big endian. */
        public final static int INT = 4;
        /* 32 bits unsigned big endian. */
        public final static int UINT = 5;
        /* 64 bits signed big endian. */
        public final static int LONG = 6;
        /* 32 bits IEEE 754. */
        public final static int FLOAT = 7;
        /* 64 bits IEEE 754. */
        public final static int DOUBLE = 8;
        /* 80 bits IEEE 754. */
        public final static int EXTENDED = 9;
        /* 8 bits ASCII. */
        public final static int CHARBYTE = 10;
        /* 16 bits UTF-16. */
        public final static int CHAR = 11;
        /* C-Style Zero terminated ASCII String. */
        public final static int CSTRING = 12;
        /* Pascal Style String. Starting with an 8 or 16 bit long length value. */
        public final static int PSTRING = 13;
        /* Pascal Style String. Starting with an 8 or 16 bit long length value.
        padded to 32 bytes. */
        public final static int PSTRING32 = 30;
        /* UTF-8 zero terminated string. */
        public final static int UTF8 = 32;
        /* UTF-16 little endian zero terminated string. */
        public final static int UTF16LE = 29;
        /* 4 ASCII characters in 4 subsequent bytes. */
        public final static int MAGIC = 14;
        /* 32 bit signed count of seconds since 1904. */
        public final static int MAC_TIMESTAMP = 15;
        /* A typedef type. */
        public final static int TYPEDEF_TYPE = 16;
        /* 16 bits signed little endian. */
        public final static int SHORTLE = 17;
        /* 16 bits unsigned little endian. */
        public final static int USHORTLE = 18;
        /* 32 bits signed little endian. */
        public final static int INTLE = 19;
        /* 32 bits unsigned little endian. */
        public final static int UINTLE = 20;
        /* 64 bits signed little endian. */
        public final static int LONGLE = 21;
        /* 2 Digits Binary Coded Decimal (=1 byte). */
        public final static int BCD2 = 22;
        /* 4 Digits Binary Coded Decimal (=2 bytes). */
        public final static int BCD4 = 23;
        /* 32-bit fixed decimal divided by 16.16 (=4 bytes). */
        public final static int FIXED_16D16 = 24;
        /* 32-bit fixed decimal divided by 2.30 (=4 bytes). */
        public final static int FIXED_2D30 = 25;
        /* 16-bit fixed decimal divided by 8.8 (=2 bytes). */
        public final static int FIXED_8D8 = 26;
        /* 1 bit unsigned. Does not need to start at byte boundaries. */
        public final static int UINT1 = 34;
        /* 2 bits unsigned. Does not need to start at byte boundaries. */
        public final static int UINT2 = 35;
        /* 3 bits unsigned. Does not need to start at byte boundaries. */
        public final static int UINT3 = 36;
        /* 4 bits unsigned. Does not need to start at byte boundaries. */
        public final static int UINT4 = 27;
        /* 5 bits unsigned. Does not need to start at byte boundaries. */
        public final static int UINT5 = 37;
        /* 5 bits unsigned. Does not need to start at byte boundaries. */
        public final static int UINT9 = 41;
        /* 8 bits unsigned. Does not need to start at byte boundaries. */
        public final static int UINT8 = 33;
        /* 16 bits unsigned. Does not need to start at byte boundaries. */
        public final static int UINT12 = 38;
        /* 16 bits unsigned. Does not need to start at byte boundaries. */
        public final static int UINT16 = 39;
        /* 31 bits unsigned. Does not need to start at byte boundaries. */
        public final static int UINT31LE = 42;
        /* 9 bits signed. Does not need to start at byte boundaries. */
        public final static int INT9 = 40;
        /* Atari 16 bit color value with 3 bits per channel:
         *      RGB 00000rrr0ggg0bbb
         */
        public final static int ATARI_COLOR = 31;
        public int type;
        public String typedef;

        public PrimitiveSpecifier(StreamPosTokenizer scanner)
                throws IOException, ParseException {

            if (scanner.nextToken() != StreamPosTokenizer.TT_WORD) {
                throw new ParseException(errorMsg("PrimitiveSpecifier: primitive type name expected", scanner));
            }

            if (scanner.sval.equals("ataricolor")) {
                type = ATARI_COLOR;
            } else if (scanner.sval.equals("byte")) {
                type = BYTE;
            } else if (scanner.sval.equals("ubyte")) {
                type = UBYTE;
            } else if (scanner.sval.equals("uint8")) {
                type = UINT8;
            } else if (scanner.sval.equals("short")) {
                type = SHORT;
            } else if (scanner.sval.equals("int16")) {
                type = SHORT;
            } else if (scanner.sval.equals("ushort")) {
                type = USHORT;
            } else if (scanner.sval.equals("uint16")) {
                type = UINT16;
            } else if (scanner.sval.equals("int")) {
                type = INT;
            } else if (scanner.sval.equals("uint")) {
                type = UINT;
            } else if (scanner.sval.equals("uint32")) {
                type = UINT;
            } else if (scanner.sval.equals("long")) {
                type = LONG;
            } else if (scanner.sval.equals("float")) {
                type = FLOAT;
            } else if (scanner.sval.equals("double")) {
                type = DOUBLE;
            } else if (scanner.sval.equals("extended")) {
                type = EXTENDED;
            } else if (scanner.sval.equals("char")) {
                type = CHAR;
            } else if (scanner.sval.equals("charbyte")) {
                type = CHARBYTE;
            } else if (scanner.sval.equals("cstring")) {
                type = CSTRING;
            } else if (scanner.sval.equals("utf8")) {
                type = UTF8;
            } else if (scanner.sval.equals("utf16le")) {
                type = UTF16LE;
            } else if (scanner.sval.equals("pstring")) {
                type = PSTRING;
            } else if (scanner.sval.equals("pstring32")) {
                type = PSTRING32;
            } else if (scanner.sval.equals("magic")) {
                type = MAGIC;
            } else if (scanner.sval.equals("mactimestamp")) {
                type = MAC_TIMESTAMP;
            } else if (scanner.sval.equals("shortLE")) {
                type = SHORTLE;
            } else if (scanner.sval.equals("ushortLE")) {
                type = USHORTLE;
            } else if (scanner.sval.equals("intLE")) {
                type = INTLE;
            } else if (scanner.sval.equals("uintLE")) {
                type = UINTLE;
            } else if (scanner.sval.equals("longLE")) {
                type = LONGLE;
            } else if (scanner.sval.equals("bcd2")) {
                type = BCD2;
            } else if (scanner.sval.equals("bcd4")) {
                type = BCD4;
            } else if (scanner.sval.equals("fixed16d16")) {
                type = FIXED_16D16;
            } else if (scanner.sval.equals("fixed2d30")) {
                type = FIXED_2D30;
            } else if (scanner.sval.equals("fixed8d8")) {
                type = FIXED_8D8;
            } else if (scanner.sval.equals("uint1")) {
                type = UINT1;
            } else if (scanner.sval.equals("uint2")) {
                type = UINT2;
            } else if (scanner.sval.equals("uint3")) {
                type = UINT3;
            } else if (scanner.sval.equals("uint4")) {
                type = UINT4;
            } else if (scanner.sval.equals("uint5")) {
                type = UINT5;
            } else if (scanner.sval.equals("uint9")) {
                type = UINT9;
            } else if (scanner.sval.equals("uint12")) {
                type = UINT12;
            } else if (scanner.sval.equals("int9")) {
                type = INT9;
            } else if (scanner.sval.equals("uint31LE")) {
                type = UINT31LE;
            } else {
                type = TYPEDEF_TYPE;
                typedef = scanner.sval;
            }
        }

        private int getResolvedPrimitveType(Declarations declarations) throws IOException {
            switch (type) {
                case TYPEDEF_TYPE:
                    TypedefDeclaration typedefDeclaration = declarations.typedefs.get(typedef);
                    if (typedefDeclaration == null) {
                        throw new IOException("typedef not found for:" + typedef);
                    }
                    return typedefDeclaration.getResolvedType(declarations);
                default:
                    return type;
            }
        }

        private Object read(ImageInputStream in, String parentIdentifier, Declarations declarations, ArrayList<StructTableModel.Value> result)
                throws IOException {
            switch (type) {
                case BYTE:
                    return new Byte(in.readByte());
                case UBYTE: {
                    int b = in.read();
                    if (b == -1) {
                        throw new EOFException();
                    }
                    return new Integer(b);
                }
                case SHORT:
                    return new Short(in.readShort());
                case USHORT:
                    return new Integer(in.readShort() & 0xffff);
                case INT:
                    return new Integer(in.readInt());
                case UINT:
                    return new Long(in.readInt() & 0xffffffffL);
                case LONG:
                    return new Long(in.readLong());
                case FLOAT:
                    return new Float(in.readFloat());
                case DOUBLE:
                    return new Double(in.readDouble());
                case EXTENDED: {
                    byte[] bits = new byte[10];
                    in.readFully(bits);
                    return new ExtendedReal(bits);
                }
                case CHARBYTE: {
                    char ch = (char) (in.readByte() & 0xff);
                    if (ch <= 0x20 || Character.isIdentifierIgnorable(ch)) {
                        return "0x" + Integer.toHexString(ch);
                    } else {
                        return new Character(ch);
                    }
                }
                case CHAR:
                    return new Character(in.readChar());
                case CSTRING: {
                    StringBuilder buf = new StringBuilder();
                    int ch;
                    while ((ch = in.read()) > 0) {
                        buf.append((char) ch);
                    }
                    return buf.toString();
                }
                case UTF8: {
                    ByteArrayOutputStream buf = new ByteArrayOutputStream();
                    int ch1;
                    while ((ch1 = in.read()) > 0) {
                        buf.write(ch1);
                    }
                    return new String(buf.toByteArray(), "UTF-8");
                }
                case UTF16LE: {
                    StringBuilder buf = new StringBuilder();
                    int ch1;
                    while ((ch1 = in.read()) > 0) {
                        int ch2 = in.read();
                        buf.append((char) (ch1 | (ch2 << 8)));
                    }
                    return buf.toString();
                }
                case PSTRING: {
                    int size = in.read();
                    if (size == 0) {
                        size = in.read();
                        in.read(); // why do we skip two bytes here?
                        in.read();
                    }
                    if (size < 0) {
                        return "";
                    }
                    byte[] bytes = new byte[size];
                    //in.readFully(bytes);
                    int n = 0;
                    while (n < size) {
                        int count = in.read(bytes, n, size - n);
                        if (count < 0) {
                            System.out.println("StructParser.PrimitiveSpecifier.read not enough bytes for pstring. Expected size:" + size + " actual size:" + n);
                            break;
                            //throw new EOFException();
                        }
                        n += count;
                    }

                    return new String(bytes);
                }
                case PSTRING32: {
                    int used = 1;
                    int size = in.read();
                    if (size > 32 - used) {
                        size = 32 - used;
                    }
                    byte[] bytes = new byte[size];
                    //in.readFully(bytes);
                    int n = 0;
                    while (n < size) {
                        int count = in.read(bytes, n, size - n);
                        if (count < 0) {
                            System.out.println("StructParser.PrimitiveSpecifier.read not enough bytes for pstring. Expected size:" + size + " actual size:" + n);
                            break;
                            //throw new EOFException();
                        }
                        n += count;
                    }
                    used += size;
                    if (used < 32) {
                        in.skipBytes(32 - used);
                    }

                    return new String(bytes);
                }
                case MAGIC: {
                    int magic = in.readInt();
                    byte[] bytes = new byte[4];
                    bytes[0] = (byte) (magic >>> 24);
                    bytes[1] = (byte) (magic >>> 16);
                    bytes[2] = (byte) (magic >>> 8);
                    bytes[3] = (byte) (magic >>> 0);
                    return new String(bytes);
                }
                case MAC_TIMESTAMP: {
                    long timestamp = ((long) in.readInt()) & 0xffffffffL;
                    DateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date date = new Date(MAC_TIMESTAMP_EPOCH + timestamp * 1000);
                    return fmt.format(date);
                }
                case TYPEDEF_TYPE: {
                    TypedefDeclaration typedefDeclaration = declarations.typedefs.get(typedef);
                    if (typedefDeclaration == null) {
                        throw new IOException("typedef not found for:" + typedef);
                    }
                    return typedefDeclaration.read(in, parentIdentifier, declarations, result);
                }
                case SHORTLE: {
                    int b0 = in.read();
                    int b1 = in.read();
                    if (b1 == -1) {
                        throw new EOFException();
                    }
                    return new Short((short) (b0 & 0xff | (b1 & 0xff) << 8));
                }
                case USHORTLE: {
                    int b0 = in.read();
                    int b1 = in.read();
                    if (b1 == -1) {
                        throw new EOFException();
                    }
                    return new Integer(b0 & 0xff | ((b1 & 0xff) << 8));
                }
                case INTLE: {
                    int b0 = in.read();
                    int b1 = in.read();
                    int b2 = in.read();
                    int b3 = in.read();
                    if (b3 == -1) {
                        throw new EOFException();
                    }
                    return new Integer(b0 & 0xff | ((b1 & 0xff) << 8) | ((b2 & 0xff) << 16) | ((b3 & 0xff) << 24));
                }
                case UINTLE: {
                    int b0 = in.read();
                    int b1 = in.read();
                    int b2 = in.read();
                    int b3 = in.read();
                    if (b3 == -1) {
                        throw new EOFException();
                    }
                    return new Long(((long) (b0 & 0xff | ((b1 & 0xff) << 8) | ((b2 & 0xff) << 16) | ((b3 & 0xff) << 24))) & 0x00ffffffff);
                }
                case LONGLE: {
                    int b0 = in.read();
                    int b1 = in.read();
                    int b2 = in.read();
                    int b3 = in.read();
                    int b4 = in.read();
                    int b5 = in.read();
                    int b6 = in.read();
                    int b7 = in.read();
                    if (b7 == -1) {
                        throw new EOFException();
                    }
                    return new Long(
                            (long) b0 & 0xff | ((long) (b1 & 0xff) << 8) | ((long) (b2 & 0xff) << 16) | ((long) (b3 & 0xff) << 24) | ((long) (b4 & 0xff) << 32) | ((long) (b5 & 0xff) << 40) | ((long) (b6 & 0xff) << 48) | ((long) (b7 & 0xff) << 56));
                }
                case BCD2: {
                    int b0 = in.read();

                    return new Integer(
                            ((b0 & 0xf0) >> 4) * 10
                            + (b0 & 0x0f));
                }
                case BCD4: {
                    int b0 = in.read();
                    int b1 = in.read();

                    return new Integer(
                            ((b0 & 0xf0) >> 4) * 1000
                            + (b0 & 0x0f) * 100
                            + ((b1 & 0xf0) >> 4) * 10
                            + (b1 & 0x0f));
                }
                case FIXED_16D16: {
                    int wholePart = in.readUnsignedShort();
                    int fractionPart = in.readUnsignedShort();

                    return new Double(wholePart + fractionPart / 65536.0);
                }
                case FIXED_2D30: {
                    int fixed = in.readInt();
                    int wholePart = fixed >>> 30;
                    int fractionPart = fixed & 0x3fffffff;

                    return new Double(wholePart + fractionPart / (double) 0x3fffffff);
                }
                case FIXED_8D8: {
                    int fixed = in.readUnsignedShort();
                    int wholePart = fixed >>> 8;
                    int fractionPart = fixed & 0xff;

                    return new Float(wholePart + fractionPart / 256f);
                }
                case UINT1: {
                    return in.readBit();
                }
                case UINT2: {
                    return (int) in.readBits(2);
                }
                case UINT3: {
                    return (int) in.readBits(3);
                }
                case UINT4: {
                    return (int) in.readBits(4);
                }
                case UINT5: {
                    return (int) in.readBits(5);
                }
                case UINT9: {
                    return (int) in.readBits(9);
                }
                case UINT8: {
                    return (int) in.readBits(8);
                }
                case UINT12: {
                    return (int) in.readBits(12);
                }
                case UINT16: {
                    return (int) in.readBits(16);
                }
                case UINT31LE: {
                    int value= (int)in.readBits(31);
                    return ((value&0xff)<<24)|((value&0xff00)<<8)|((value&0xff0000)>>>8)|((value&0xff000000)>>>24);
                }
                case INT9: {
                    int value = (int) in.readBits(9);
                    if ((value & 0x256) != 0) {
                        value |= 0xffffff00;
                    }
                    return value;
                }
                case ATARI_COLOR: {
                    int clr = in.readUnsignedShort();
                    int red = (clr & 0x700) >> 8;
                    int green = (clr & 0x70) >> 4;
                    int blue = (clr & 0x7);
                    StringBuilder buf = new StringBuilder();
                    String hex = Integer.toHexString(clr);
                    buf.append("0x");
                    for (int i = 0; i < 4 - hex.length(); i++) {
                        buf.append('0');
                    }
                    buf.append(hex);
                    buf.append(" {rgb:0x");
                    int rgb = ((red << 5) | (red << 2) | (red >>> 1)) << 16//
                            | ((green << 5) | (green << 2) | (green >>> 1)) << 8
                            | ((blue << 5) | (blue << 2) | (blue >>> 1));
                    hex = Integer.toHexString(rgb);
                    for (int i = 0; i < 6 - hex.length(); i++) {
                        buf.append('0');
                    }
                    buf.append(hex);
                    buf.append(", rgb:");
                    buf.append((red << 5) | (red << 2) | (red >>> 1));
                    buf.append(' ');
                    buf.append((green << 5) | (green << 2) | (green >>> 1));
                    buf.append(' ');
                    buf.append((blue << 5) | (blue << 2) | (blue >>> 1));
                    buf.append('}');
                    return new String(buf);
                }
                default:
                    throw new InternalError("invalid type:" + type);
            }
        }
    }

    /**
     * ArraySize expression.
     *
     * <pre><code>
     * ArraySize ::= () | intLiteral | identifier [("-"|"+"|"=="|"!=") intLiteral]
     * </code></pre>
     */
    protected static class ArraySize {

        public final static int REMAINDER = -1;
        public final static int LITERAL = 0;
        public final static int VARIABLE = 1;
        public final static int NEGATIVE_VARIABLE = 2;
        public final static int EQUAL = 3;
        public final static int NOT_EQUAL = 4;
        public int type = LITERAL;
        public int size;
        public String variable;
        public int offset;
        public Object equal;

        public ArraySize(StreamPosTokenizer scanner)
                throws IOException, ParseException {
            if (scanner.nextToken() == ']') {
                scanner.pushBack();
                type = REMAINDER;
            } else if (scanner.ttype == StreamPosTokenizer.TT_WORD) {
                variable = scanner.sval;
                if (scanner.nextToken() == '-') {
                    offset = new IntLiteral(scanner).intValue() * -1;
                    type = VARIABLE;
                } else if (scanner.ttype == '+') {
                    offset = new IntLiteral(scanner).intValue();
                    type = VARIABLE;
                } else if (scanner.ttype == '=') {
                    if (scanner.nextToken() != '=') {
                        throw new ParseException(errorMsg("== expected", scanner));
                    }
                    if (scanner.nextToken() == '"') {
                        equal = scanner.sval;
                    } else {
                        scanner.pushBack();
                        equal = new IntLiteral(scanner).intValue();
                    }
                    type = EQUAL;
                } else if (scanner.ttype == '!') {
                    if (scanner.nextToken() != '=') {
                        throw new ParseException(errorMsg("!= expected", scanner));
                    }
                    if (scanner.nextToken() == '"') {
                        equal = scanner.sval;
                    } else {
                        int sign;
                        if (scanner.ttype=='-') {
                            sign=-1;
                        }else {
                            sign=1;
                        scanner.pushBack();
                        }
                        equal = new IntLiteral(scanner).intValue()*sign;
                    }
                    type = NOT_EQUAL;
                } else {
                    type = VARIABLE;
                    scanner.pushBack();
                }
            } else {
                scanner.pushBack();
                size = new IntLiteral(scanner).intValue();
                if (scanner.nextToken() == '-') {
                    offset = size;
                    size = 0;
                    if (scanner.nextToken() != StreamPosTokenizer.TT_WORD) {
                        throw new ParseException(errorMsg("Variable name expected", scanner));
                    }
                    variable = scanner.sval;
                    type = NEGATIVE_VARIABLE;
                } else if (scanner.ttype == '+') {
                    offset = size;
                    size = 0;
                    if (scanner.nextToken() != StreamPosTokenizer.TT_WORD) {
                        throw new ParseException(errorMsg("Variable name expected", scanner));
                    }
                    variable = scanner.sval;
                    type = VARIABLE;
                } else if (scanner.ttype == '=') {
                    if (scanner.nextToken() != '=') {
                        throw new ParseException(errorMsg("== expected", scanner));
                    }
                    offset = size;
                    size = 0;
                    if (scanner.nextToken() != StreamPosTokenizer.TT_WORD) {
                        throw new ParseException(errorMsg("Variable name expected", scanner));
                    }
                    variable = scanner.sval;
                    type = EQUAL;
                } else if (scanner.ttype == '!') {
                    if (scanner.nextToken() != '=') {
                        throw new ParseException(errorMsg("== expected", scanner));
                    }
                    offset = size;
                    size = 0;
                    if (scanner.nextToken() != StreamPosTokenizer.TT_WORD) {
                        throw new ParseException(errorMsg("Variable name expected", scanner));
                    }
                    variable = scanner.sval;
                    type = NOT_EQUAL;
                } else {
                    type = LITERAL;
                    scanner.pushBack();
                }
            }
        }

        public int getArraySize(Declarations declarations, ArrayList<StructTableModel.Value> result) {
            switch (type) {
                case LITERAL:
                    return size;
                case VARIABLE:
                case NEGATIVE_VARIABLE:
                    int sign = type == NEGATIVE_VARIABLE ? -1 : 1;

                    // Search for variable with the same name
                    for (int i = result.size() - 1; i > -1; i--) {
                        StructTableModel.Value value = result.get(i);
                        if (value.declaration.equals(variable)) {
                            int variableValue = ((Number) value.value).intValue();
                            return variableValue * sign + offset;
                        }
                    }
                    // Search for fully qualified variable with the same name
                    for (int i = result.size() - 1; i > -1; i--) {
                        StructTableModel.Value value = result.get(i);
                        if (value.qualifiedIdentifier.equals(variable)) {
                            int variableValue = ((Number) value.value).intValue();
                            return variableValue * sign + offset;
                        }
                    }
                    // Search for fully qualified variable with the same name ending
                    for (int i = result.size() - 1; i > -1; i--) {
                        StructTableModel.Value value = result.get(i);
                        if (value.qualifiedIdentifier.endsWith(variable)) {
                            int variableValue = ((Number) value.value).intValue();
                            return variableValue * sign + offset;
                        }
                    }
                    throw new InternalError("Invalid ArraySize variable:" + variable);
                case EQUAL:
                case NOT_EQUAL:
                    int trueValue = type == EQUAL ? 1 : 0;
                    int falseValue = 1 - trueValue;

                    // Search for variable with the same name
                    for (int i = result.size() - 1; i > -1; i--) {
                        StructTableModel.Value value = result.get(i);
                        if (value.declaration.equals(variable)) {
                            return ((Number)equal).intValue()==((Number)value.value).intValue()? trueValue : falseValue;
                        }
                    }
                    // Search for fully qualified variable with the same name
                    for (int i = result.size() - 1; i > -1; i--) {
                        StructTableModel.Value value = result.get(i);
                        if (value.qualifiedIdentifier.equals(variable)) {
                            return ((Number)equal).intValue()==((Number)value.value).intValue() ? trueValue : falseValue;
                        }
                    }
                    // Search for fully qualified variable with the same name ending
                    for (int i = result.size() - 1; i > -1; i--) {
                        StructTableModel.Value value = result.get(i);
                        if (value.qualifiedIdentifier.endsWith(variable)) {
                            return ((Number)equal).intValue()==((Number)value.value).intValue() ? trueValue : falseValue;
                        }
                    }
                    throw new InternalError("Invalid ArraySize variable:" + variable);
                case REMAINDER:
                    return -1;
                default:
                    throw new InternalError("Invalid ArraySize type:" + type);
            }
        }
    }

    /**
     * IntLiteral expression.
     *
     * <pre><code>
     * IntLiteral ::= intLiteral | hexLiteral
     * </code></pre>
     */
    protected static class IntLiteral {

        public int value;

        public IntLiteral(StreamPosTokenizer scanner)
                throws IOException, ParseException {
            if (scanner.nextToken() != StreamPosTokenizer.TT_NUMBER) {
                throw new ParseException(errorMsg("IntLiteral: numeric value expected", scanner));
            }
            value = (int)((long) scanner.nval);// Must cast like this to get proper signs
            if (scanner.nval == 0.0) {
                if (scanner.nextToken() == StreamPosTokenizer.TT_WORD && scanner.sval.startsWith("x")) {
                    value = Integer.valueOf(scanner.sval.substring(1), 16).intValue();
                } else {
                    scanner.pushBack();
                }
            }
        }

        public int intValue() {
            return value;
        }
    }

    /**
     * MagicOrIntLiteral expression.
     *
     * <pre><code>
     * MagicOrIntLiteral ::= magicLiteral | intLiteral | hexLiteral
     * </code></pre>
     */
    protected static class MagicOrIntLiteral {

        public int intValue;
        public String magicValue;

        public MagicOrIntLiteral(StreamPosTokenizer scanner)
                throws IOException, ParseException {
            switch (scanner.nextToken()) {
                case StreamPosTokenizer.TT_NUMBER:
                    intValue = (int) scanner.nval;
                    if (scanner.nval == 0.0) {
                        if (scanner.nextToken() == StreamPosTokenizer.TT_WORD && scanner.sval.startsWith("x")) {
                            intValue = Integer.valueOf(scanner.sval.substring(1), 16).intValue();
                        } else {
                            scanner.pushBack();
                        }
                    }
                    break;
                case '-':
                    if (scanner.nextToken() != StreamPosTokenizer.TT_NUMBER) {
                        throw new ParseException(errorMsg("MagicOrIntLiteral: numeric value expected", scanner));
                    }
                    intValue = -(int) scanner.nval;
                    break;
                case '"':
                    if (scanner.sval.length() != 4) {
                        throw new ParseException(errorMsg("MagicOrIntLiteral: magic with length of 4 characters expected", scanner));
                    }
                    magicValue = scanner.sval;
                    intValue = toInt(magicValue);
                    break;
                default:
                    throw new ParseException(errorMsg("MagicOrIntLiteral: numeric value expected", scanner));
            }
        }

        public boolean isMagic() {
            return magicValue != null;
        }

        public int intValue() {
            return intValue;
        }

        public String stringValue() {
            return (magicValue == null) ? Integer.toString(intValue) : magicValue;
        }

        public static int toInt(String magic) {
            return ((magic.charAt(0) & 0xff) << 24) | ((magic.charAt(1) & 0xff) << 16) | ((magic.charAt(2) & 0xff) << 8) | ((magic.charAt(3) & 0xff) << 0);
        }

        public static String toMagic(int value) {
            byte[] buf = new byte[4];
            buf[0] = (byte) ((value & 0xff000000) >>> 24);
            buf[1] = (byte) ((value & 0xff0000) >>> 16);
            buf[2] = (byte) ((value & 0xff00) >>> 8);
            buf[3] = (byte) (value & 0xff);
            try {
                return new String(buf, "ASCII");
            } catch (UnsupportedEncodingException e) {
                InternalError error = new InternalError(e.getMessage());
                error.initCause(e);
                throw error;
            }
        }
    }
}
