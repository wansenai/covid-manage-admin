package com.summer.common.helper;

import com.alibaba.fastjson.JSONObject;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class StrCastHelper {
    private static final String BLANK = "";

    private StrCastHelper() {
    }

    /**
     * Form Data To Object
     **/
    public static <T> T form2Bean(String parameters, Class<T> clazz) {
        return BeanHelper.map2Bean(form2Json(parameters), clazz);
    }

    /**
     * Form Data to json
     **/
    public static JSONObject form2Json(String parameters) {
        try {
            if (StringHelper.isBlank(parameters)) {
                return new JSONObject();
            }
            MultiMap multiMap = new MultiMap();
            decodeTo(parameters, multiMap);
            return new JSONObject(multiMap);
        } catch (Exception e) {
            throw new RuntimeException("form parameters to json error....", e);
        }
    }

    /**
     * XML Data to json
     **/
    public static JSONObject xml2Json(String xml) {
        try {
            Document document = DocumentHelper.parseText(xml);
            MultiMap multiMap = new MultiMap();
            recursiveNode(multiMap, document.getRootElement());
            return new JSONObject(multiMap);
        } catch (Exception e) {
            throw new RuntimeException("xml parameters to json error....", e);
        }
    }

    private static void recursiveNode(MultiMap multiMap, Element node) {
        multiMap.add(node.getName(), node.getTextTrim());
        //递归遍历当前节点所有的子节点
        @SuppressWarnings("unchecked")
        List<Element> elements = node.elements();
        for (Element element : elements) {
            recursiveNode(multiMap, element);
        }
    }

    private static void decodeTo(String content, MultiMap map) {
        String key = null, value;
        int mark = -1;
        boolean encoded = false;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            switch (c) {
                case '&':
                    int l = i - mark - 1;
                    value = l == 0 ? BLANK : (encoded ? decodeString(content, (mark + 1), l) : content.substring(mark + 1, i));
                    mark = i;
                    encoded = false;
                    if (key != null) {
                        map.add(key, value);
                    } else if (value != null && value.length() > 0) {
                        map.add(value, BLANK);
                    }
                    key = null;
                    break;
                case '=':
                    if (key != null)
                        break;
                    key = encoded ? decodeString(content, (mark + 1), (i - mark - 1)) : content.substring(mark + 1, i);
                    mark = i;
                    encoded = false;
                    break;
                case '+':
                    encoded = true;
                    break;
                case '%':
                    encoded = true;
                    break;
            }
        }
        if (key != null) {
            int l = content.length() - mark - 1;
            value = l == 0 ? BLANK : (encoded ? decodeString(content, (mark + 1), l) : content.substring(mark + 1));
            map.add(key, value);
        } else if (mark < content.length()) {
            key = encoded ? decodeString(content, (mark + 1), (content.length() - mark - 1)) : content.substring(mark + 1);
            map.add(key, BLANK);
        }
    }

    private static String decodeString(String encoded, int offset, int length) {
        Utf8StringBuffer buffer = null;
        for (int i = 0; i < length; i++) {
            char c = encoded.charAt(offset + i);
            if (c > 0xff) {
                if (buffer == null) {
                    buffer = new Utf8StringBuffer(length);
                    buffer.getStringBuffer().append(encoded, offset, (offset + i + 1));
                } else
                    buffer.getStringBuffer().append(c);
            } else if (c == '+') {
                if (buffer == null) {
                    buffer = new Utf8StringBuffer(length);
                    buffer.getStringBuffer().append(encoded, offset, (offset + i));
                }

                buffer.getStringBuffer().append(' ');
            } else if (c == '%' && (i + 2) < length) {
                if (buffer == null) {
                    buffer = new Utf8StringBuffer(length);
                    buffer.getStringBuffer().append(encoded, offset, (offset + i));
                }

                while (c == '%' && (i + 2) < length) {
                    try {
                        byte b = (byte) parseInt(encoded, (offset + i + 1));
                        buffer.append(b);
                        i += 3;
                    } catch (NumberFormatException nfe) {
                        buffer.getStringBuffer().append('%');
                        for (char next; ((next = encoded.charAt(++i + offset)) != '%'); )
                            buffer.getStringBuffer().append((next == '+' ? ' ' : next));
                    }

                    if (i < length)
                        c = encoded.charAt(offset + i);
                }
                i--;
            } else if (buffer != null)
                buffer.getStringBuffer().append(c);
        }
        if (buffer == null) {
            if (offset == 0 && encoded.length() == length) {
                return encoded;
            }
            return encoded.substring(offset, offset + length);
        }
        return buffer.toString();
    }

    private static int parseInt(String s, int offset) throws NumberFormatException {
        int value = 0, base = 16;
        for (int i = 0; i < 2; i++) {
            char c = s.charAt(offset + i);
            int digit = c - '0';
            if (digit < 0 || digit >= base || digit >= 10) {
                digit = 10 + c - 'A';
                if (digit < 10 || digit >= base) digit = 10 + c - 'a';
            }
            if (digit < 0 || digit >= base)
                throw new NumberFormatException(s.substring(offset, offset + 2));
            value = value * base + digit;
        }
        return value;
    }

    private static class Utf8StringBuffer {
        StringBuffer _buffer;
        int _more, _bits;
        boolean _errors;

        Utf8StringBuffer(int capacity) {
            _buffer = new StringBuffer(capacity);
        }

        void append(byte b) {
            if (b >= 0) {
                if (_more > 0) {
                    _buffer.append('?');
                    _more = 0;
                    _bits = 0;
                } else {
                    _buffer.append((char) (0x7f & b));
                }
            } else if (_more == 0) {
                if ((b & 0xc0) != 0xc0) {
                    _buffer.append('?');
                    _more = 0;
                    _bits = 0;
                } else if ((b & 0xe0) == 0xc0) {
                    _more = 1;
                    _bits = b & 0x1f;
                } else if ((b & 0xf0) == 0xe0) {
                    _more = 2;
                    _bits = b & 0x0f;
                } else if ((b & 0xf8) == 0xf0) {
                    _more = 3;
                    _bits = b & 0x07;
                } else if ((b & 0xfc) == 0xf8) {
                    _more = 4;
                    _bits = b & 0x03;
                } else if ((b & 0xfe) == 0xfc) {
                    _more = 5;
                    _bits = b & 0x01;
                }
            } else {
                if ((b & 0xc0) == 0xc0) {
                    _buffer.append('?');
                    _more = 0;
                    _bits = 0;
                    _errors = true;
                } else {
                    _bits = (_bits << 6) | (b & 0x3f);
                    if (--_more == 0)
                        _buffer.append((char) _bits);
                }
            }
        }

        StringBuffer getStringBuffer() {
            return _buffer;
        }

        boolean isError() {
            return _errors || _more > 0;
        }

        public String toString() {
            return _buffer.toString();
        }
    }

    private static class MultiMap extends HashMap<String, Object> implements Cloneable {
        MultiMap() {
        }

        void add(String name, Object value) {
            Object lo = super.get(name);
            Object ln = LazyList.add(lo, value);
            if (lo != ln) super.put(name, ln);
        }
    }

    private static class LazyList implements Cloneable, Serializable {
        private LazyList() {
        }

        static Object add(Object list, Object item) {
            if (list == null) {
                if (item instanceof List || item == null) {
                    List l = new ArrayList();
                    //noinspection unchecked
                    l.add(item);
                    return l;
                }
                return item;
            }
            if (list instanceof List) {
                //noinspection unchecked
                ((List) list).add(item);
                return list;
            }
            List l = new ArrayList();
            //noinspection unchecked
            l.add(list);
            //noinspection unchecked
            l.add(item);
            return l;
        }
    }
}
