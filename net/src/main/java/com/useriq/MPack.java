/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Achille Roussel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.useriq;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MPack {
    private static final int NIL = 0xc0;
    private static final int FALSE = 0xc2;
    private static final int TRUE = 0xc3;
    private static final int BIN8 = 0xc4;
    private static final int BIN16 = 0xc5;
    private static final int BIN32 = 0xc6;
    private static final int EXT8 = 0xc7;
    private static final int EXT16 = 0xc8;
    private static final int EXT32 = 0xc9;
    private static final int FLOAT32 = 0xca;
    private static final int FLOAT64 = 0xcb;
    private static final int UINT8 = 0xcc;
    private static final int UINT16 = 0xcd;
    private static final int UINT32 = 0xce;
    private static final int UINT64 = 0xcf;
    private static final int INT8 = 0xd0;
    private static final int INT16 = 0xd1;
    private static final int INT32 = 0xd2;
    private static final int INT64 = 0xd3;
    private static final int FIXEXT1 = 0xd4;
    private static final int FIXEXT2 = 0xd5;
    private static final int FIXEXT4 = 0xd6;
    private static final int FIXEXT8 = 0xd7;
    private static final int FIXEXT16 = 0xd8;
    private static final int STR8 = 0xd9;
    private static final int STR16 = 0xda;
    private static final int STR32 = 0xdb;
    private static final int ARRAY16 = 0xdc;
    private static final int ARRAY32 = 0xdd;
    private static final int MAP16 = 0xde;
    private static final int MAP32 = 0xdf;
    private static final int FIXARRAY = 0x90;
    private static final int FIXSTR = 0xa0;
    private static final int FIXMAP = 0x80;

    private MPack() {
    }

    public static Object decode(byte[] bytes) throws IOException {
        return decode(new ByteArrayInputStream(bytes));
    }

    public static Object decode(InputStream istream) throws IOException {
        return (new Decoder(istream)).decode();
    }

    public static byte[] encode(Object object) throws IOException {
        final ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        encode(object, ostream);
        return ostream.toByteArray();
    }

    public static void encode(Object object, OutputStream ostream) throws IOException {
        final Encoder encoder = new Encoder(ostream);
        encoder.encode(object);
        encoder.flush();
    }

    private static class FIXNUM {
        private static final int POSITIVE = 0x00;
        private static final int NEGATIVE = 0xe0;
    }

    public static class Extended {
        public int type;
        public byte[] data;

        public Extended(int type, byte[] data) {
            this.type = type;
            this.data = data;
        }
    }

    public static class Decoder {
        public final DataInputStream istream;

        public Decoder(InputStream istream) {
            this.istream = new DataInputStream(istream);
        }

        private String decodeString(int length) throws IOException {
            final byte[] bytes = new byte[length];
            this.istream.readFully(bytes);
            return new String(bytes, "UTF-8");
        }

        private byte[] decodeBinary(int length) throws IOException {
            final byte[] bytes = new byte[length];
            this.istream.readFully(bytes);
            return bytes;
        }

        private List<?> decodeArray(int length) throws IOException {
            final ArrayList<Object> array = new ArrayList<Object>(length);
            while (length-- != 0) {
                array.add(this.decode());
            }
            return array;
        }

        private Map<?, ?> decodeMap(int length) throws IOException {
            final HashMap<Object, Object> map = new HashMap<Object, Object>();
            while (length-- != 0) {
                final Object key = this.decode();
                final Object val = this.decode();
                map.put(key, val);
            }
            return map;
        }

        private Extended decodeExtended(int length) throws IOException {
            final byte[] data = new byte[length];
            final int type = this.istream.readUnsignedByte();
            this.istream.readFully(data);
            return new Extended(type, data);
        }

        public final Object decode() throws IOException {
            final int tag = this.istream.readUnsignedByte();

            if ((tag & 0x80) == FIXNUM.POSITIVE) return (long) ((byte) tag);
            if ((tag & 0xE0) == FIXNUM.NEGATIVE) return (long) ((byte) tag);

            if ((tag & 0xE0) == FIXSTR) {
                final int length = tag & ~FIXSTR;
                final byte[] bytes = new byte[length];
                this.istream.readFully(bytes);
                return new String(bytes, "UTF-8");
            }

            if ((tag & 0xF0) == FIXARRAY) return this.decodeArray(tag & ~FIXARRAY);
            if ((tag & 0xF0) == FIXMAP) return this.decodeMap(tag & ~FIXMAP);

            switch (tag) {
                case NIL:
                    return null;
                case TRUE:
                    return true;
                case FALSE:
                    return false;
                case UINT8:
                    return (long) this.istream.readUnsignedByte();
                case UINT16:
                    return (long) this.istream.readUnsignedShort();
                case UINT32:
                    final long upper = this.istream.readUnsignedShort();
                    final long lower = this.istream.readUnsignedShort();
                    return (upper << 16) | lower;
                case UINT64:
                    return this.istream.readLong();
                case INT8:
                    return (long) this.istream.readByte();
                case INT16:
                    return (long) this.istream.readShort();
                case INT32:
                    return (long) this.istream.readInt();
                case INT64:
                    return this.istream.readLong();
                case FLOAT32:
                    return this.istream.readFloat();
                case FLOAT64:
                    return this.istream.readDouble();
                case STR8:
                    return this.decodeString(this.istream.readUnsignedByte());
                case STR16:
                    return this.decodeString(this.istream.readUnsignedShort());
                case STR32:
                    return this.decodeString(this.istream.readInt());
                case BIN8:
                    return this.decodeBinary(this.istream.readUnsignedByte());
                case BIN16:
                    return this.decodeBinary(this.istream.readUnsignedShort());
                case BIN32:
                    return this.decodeBinary(this.istream.readInt());
                case ARRAY16:
                    return this.decodeArray(this.istream.readUnsignedShort());
                case ARRAY32:
                    return this.decodeArray(this.istream.readInt());
                case MAP16:
                    return this.decodeMap(this.istream.readUnsignedShort());
                case MAP32:
                    return this.decodeMap(this.istream.readInt());
                case FIXEXT1:
                    return this.decodeExtended(1);
                case FIXEXT2:
                    return this.decodeExtended(2);
                case FIXEXT4:
                    return this.decodeExtended(4);
                case FIXEXT8:
                    return this.decodeExtended(8);
                case FIXEXT16:
                    return this.decodeExtended(16);
                case EXT8:
                    return this.decodeExtended(this.istream.readUnsignedByte());
                case EXT16:
                    return this.decodeExtended(this.istream.readUnsignedShort());
                case EXT32:
                    return this.decodeExtended(this.istream.readInt());
                default:
                    throw new IOException("MPack: decoder found unknown tag: " + tag);
            }
        }
    }

    public static class Encoder {
        public final DataOutputStream ostream;

        public Encoder(OutputStream ostream) {
            this.ostream = new DataOutputStream(ostream);
        }

        private void encodeInteger(long object) throws IOException {
            if (object >= 0L) {
                if (object <= 127L) {
                    this.ostream.writeByte((int) object);
                } else if (object <= 255L) {
                    this.ostream.writeByte(UINT8);
                    this.ostream.writeByte((int) object);
                } else if (object <= 65535L) {
                    this.ostream.writeByte(UINT16);
                    this.ostream.writeShort((int) object);
                } else if (object <= 4294967295L) {
                    this.ostream.writeByte(UINT32);
                    this.ostream.writeInt((int) object);
                } else {
                    this.ostream.writeByte(UINT64);
                    this.ostream.writeLong(object);
                }
            } else {
                if (object >= -31L) {
                    this.ostream.writeByte((int) object);
                } else if (object >= -128L) {
                    this.ostream.writeByte(INT8);
                    this.ostream.writeByte((int) object);
                } else if (object >= -32768L) {
                    this.ostream.writeByte(INT16);
                    this.ostream.writeShort((int) object);
                } else if (object >= -2147483648L) {
                    this.ostream.writeByte(INT32);
                    this.ostream.writeInt((int) object);
                } else {
                    this.ostream.writeByte(INT64);
                    this.ostream.writeLong(object);
                }
            }
        }

        private void encodeExtendedDataType(Extended object) throws IOException {
            this.ostream.writeByte(object.type);
            this.ostream.write(object.data);
        }

        public final void encode(Object object) throws IOException {
            if (object == null) this.ostream.writeByte(NIL);
            else if (object instanceof Boolean)
                this.ostream.writeByte((boolean) (Boolean) object ? TRUE : FALSE);
            else if (object instanceof Byte) this.encodeInteger((long) (byte) (Byte) object);
            else if (object instanceof Short) this.encodeInteger((long) (short) (Short) object);
            else if (object instanceof Integer) this.encodeInteger((long) (int) (Integer) object);
            else if (object instanceof Long) this.encodeInteger((Long) object);
            else if (object instanceof Float) {
                this.ostream.writeByte(FLOAT32);
                this.ostream.writeFloat((Float) object);
            } else if (object instanceof Double) {
                this.ostream.writeByte(FLOAT64);
                this.ostream.writeDouble((Double) object);
            } else if (object instanceof String) {
                byte[] object1 = ((String) object).getBytes("UTF-8");
                if (object1.length <= 15) {
                    this.ostream.writeByte(FIXSTR | object1.length);
                    this.ostream.write(object1);
                } else if (object1.length <= 255) {
                    this.ostream.writeByte(STR8);
                    this.ostream.writeByte(object1.length);
                    this.ostream.write(object1);
                } else if (object1.length <= 65535) {
                    this.ostream.writeByte(STR16);
                    this.ostream.writeShort(object1.length);
                    this.ostream.write(object1);
                } else {
                    this.ostream.writeByte(STR32);
                    this.ostream.writeInt(object1.length);
                    this.ostream.write(object1);
                }
            } else if (object instanceof byte[]) {
                if (((byte[]) object).length <= 255) {
                    this.ostream.writeByte(BIN8);
                    this.ostream.writeByte(((byte[]) object).length);
                    this.ostream.write((byte[]) object);
                } else if (((byte[]) object).length <= 65535) {
                    this.ostream.writeByte(BIN16);
                    this.ostream.writeShort(((byte[]) object).length);
                    this.ostream.write((byte[]) object);
                } else {
                    this.ostream.writeByte(BIN32);
                    this.ostream.writeInt(((byte[]) object).length);
                    this.ostream.write((byte[]) object);
                }
            } else if (object instanceof List<?>) {
                final int length = ((List<?>) object).size();
                if (length <= 15)
                    this.ostream.writeByte(FIXARRAY | length);
                else if (length <= 65535) {
                    this.ostream.writeByte(ARRAY16);
                    this.ostream.writeShort(length);
                } else {
                    this.ostream.writeByte(ARRAY32);
                    this.ostream.writeInt(length);
                }

                for (Object item : (List<?>) object)
                    this.encode(item);
            } else if (object instanceof Map<?, ?>) {
                final int length = ((Map<?, ?>) object).size();
                if (length <= 15) {
                    this.ostream.writeByte(FIXMAP | length);
                } else if (length <= 65535) {
                    this.ostream.writeByte(MAP16);
                    this.ostream.writeShort(length);
                } else {
                    this.ostream.writeByte(MAP32);
                    this.ostream.writeInt(length);
                }
                for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
                    this.encode(entry.getKey());
                    this.encode(entry.getValue());
                }
            } else if (object instanceof Extended) {
                switch (((Extended) object).data.length) {
                    case 1:
                        this.ostream.writeByte(FIXEXT1);
                        this.encodeExtendedDataType((Extended) object);
                        break;
                    case 2:
                        this.ostream.writeByte(FIXEXT2);
                        this.encodeExtendedDataType((Extended) object);
                        break;
                    case 4:
                        this.ostream.writeByte(FIXEXT4);
                        this.encodeExtendedDataType((Extended) object);
                        break;
                    case 8:
                        this.ostream.writeByte(FIXEXT8);
                        this.encodeExtendedDataType((Extended) object);
                        break;
                    case 16:
                        this.ostream.writeByte(FIXEXT16);
                        this.encodeExtendedDataType((Extended) object);
                        break;
                    default:
                        if (((Extended) object).data.length <= 255) {
                            this.ostream.writeByte(EXT8);
                            this.ostream.writeByte(((Extended) object).data.length);
                            this.encodeExtendedDataType((Extended) object);
                        } else if (((Extended) object).data.length <= 65535) {
                            this.ostream.writeByte(EXT16);
                            this.ostream.writeShort(((Extended) object).data.length);
                            this.encodeExtendedDataType((Extended) object);
                        } else {
                            this.ostream.writeByte(EXT32);
                            this.ostream.writeInt(((Extended) object).data.length);
                            this.encodeExtendedDataType((Extended) object);
                        }
                }
            } else
                throw new IllegalArgumentException("MPack: no encoding available for objects of type " + object.getClass().toString());
        }

        public final void flush() throws IOException {
            this.ostream.flush();
        }
    }
}