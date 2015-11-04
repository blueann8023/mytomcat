package com.lm.mytomcat.chap3.connector.http;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import lombok.extern.slf4j.Slf4j;

import org.apache.catalina.util.StringManager;

@Slf4j
public class SocketInputStream extends InputStream {
    // constants
    private static final byte CR = (byte) '\r';
    private static final byte LF = (byte) '\n';
    private static final byte SP = (byte) ' ';
    private static final byte HT = (byte) '\t';
    private static final byte COLON = (byte) ':';
    private static final int LC_OFFSET = 'A' - 'a';// Lower case offset.

    // Underlying input stream.construtor
    protected InputStream inputStream;

    protected byte buf[];// buffer . construtor instance
    protected int count;// Last valid byte in buffer.
    protected int pos;// Position in the buffer.

    protected static StringManager sm = StringManager.getManager(Constants.Package);

    public SocketInputStream(InputStream inputStream, int bufferSize) {
        this.inputStream = inputStream;
        buf = new byte[bufferSize];
    }

    public void readHttpRequestLine(HttpRequestLine httpRequestLine) throws IOException {

        // recyling check 避免HttpRequestLine在其他地方被改写了 确保是ok的
        if (httpRequestLine.methodEnd != 0) {
            httpRequestLine.recycle();
        }

        // check for a blank line
        int chr = 0;
        do {// skip cr lf
            try {
                chr = read();
                log.debug("SocketInputStream readHttpRequestLine blank chr {}", chr);
            } catch (IOException e) {
                chr = -1;
            }
        } while (chr == CR || chr == LF);

        if (chr == -1) {
            throw new EOFException(sm.getString("requestStream.readline.error"));
        }

        pos--;// ??

        // read the method name
        int maxRead = httpRequestLine.method.length;
        int readStart = pos;
        int readCount = 0;

        boolean space = false;

        while (!space) {
            if (readCount >= maxRead) {
                if ((2 * maxRead) < HttpRequestLine.MAX_METHOD_SIZE) {
                    char[] newBuffer = new char[2 * maxRead];
                    System.arraycopy(httpRequestLine.method, 0, newBuffer, 0, maxRead);
                    httpRequestLine.method = newBuffer;
                    maxRead = httpRequestLine.method.length;
                } else {
                    throw new IOException(sm.getString("requestStream.readline.toolong"));
                }
            }

            if (pos >= count) {
                int val = read();
                log.debug("SocketInputStream readHttpRequestLine method chr {}", chr);
                if (val == -1) {
                    throw new IOException(sm.getString("requestStream.readline.error"));
                }
                pos = 0;
                readStart = 0;
            }

            if (buf[pos] == SP) {
                space = true;
            }

            httpRequestLine.method[readCount] = (char) buf[pos];
            readCount++;
            pos++;
        }

        httpRequestLine.methodEnd = readCount - 1;

        // read uri
        maxRead = httpRequestLine.uri.length;
        readStart = pos;
        readCount = 0;

        space = false;

        boolean eol = false;

        while (!space) {
            if (readCount >= maxRead) {
                if ((2 * maxRead) <= HttpRequestLine.MAX_URI_SIZE) {
                    char[] newBuffer = new char[2 * maxRead];
                    System.arraycopy(httpRequestLine.uri, 0, newBuffer, 0, maxRead);
                    httpRequestLine.uri = newBuffer;
                    maxRead = httpRequestLine.uri.length;
                } else {
                    throw new IOException(sm.getString("requestStream.readline.toolong"));
                }
            }
            // We're at the end of the internal buffer
            if (pos >= count) {
                int val = read();
                log.debug("SocketInputStream readHttpRequestLine uri chr {}", val);
                if (val == -1)
                    throw new IOException(sm.getString("requestStream.readline.error"));
                pos = 0;
                readStart = 0;
            }
            if (buf[pos] == SP) {
                space = true;
            } else if ((buf[pos] == CR) || (buf[pos]) == LF) {
                // HTTP/0.9 style request
                eol = true;
                space = true;
            }

            httpRequestLine.uri[readCount] = (char) buf[pos];
            readCount++;
            pos++;
        }
        httpRequestLine.uriEnd = readCount - 1;

        // read protocol
        maxRead = httpRequestLine.protocol.length;
        readStart = pos;
        readCount = 0;

        while (!eol) {
            // if the buffer is full, extend it
            if (readCount >= maxRead) {
                if ((2 * maxRead) <= HttpRequestLine.MAX_PROTOCOL_SIZE) {
                    char[] newBuffer = new char[2 * maxRead];
                    System.arraycopy(httpRequestLine.protocol, 0, newBuffer, 0, maxRead);
                    httpRequestLine.protocol = newBuffer;
                    maxRead = httpRequestLine.protocol.length;
                } else {
                    throw new IOException(sm.getString("requestStream.readline.toolong"));
                }
            }
            // We're at the end of the internal buffer
            if (pos >= count) {
                // Copying part (or all) of the internal buffer to the line
                // buffer
                int val = read();
                log.debug("SocketInputStream readHttpRequestLine protocol chr {}", val);
                if (val == -1)
                    throw new IOException(sm.getString("requestStream.readline.error"));
                pos = 0;
                readStart = 0;
            }
            if (buf[pos] == CR) {
                // Skip CR.
            } else if (buf[pos] == LF) {
                eol = true;
            } else {
                httpRequestLine.protocol[readCount] = (char) buf[pos];
                readCount++;
            }
            pos++;
        }

        httpRequestLine.protocolEnd = readCount;
    }

    public void readHeader(HttpHeader header) throws IOException {
        log.debug("SocketInputStream readHeader Start");
        // recycling check
        if (header.nameEnd != 0)
            header.recycle();

        // checking for a blank line
        int chr = read();
        log.debug("SocketInputStream readHeader blank line chr {}", chr);
        if ((chr == CR) || (chr == LF)) {
            if (chr == CR)
                read();
            header.nameEnd = 0;
            header.valueEnd = 0;
            return;
        } else {
            pos--;
        }

        // reading the header name
        int maxRead = header.name.length;
        int readStart = pos;
        int readCount = 0;

        boolean colon = false;
        while (!colon) {
            log.debug("SocketInputStream readHeader headname while Start");
            if (readCount >= maxRead) {
                if (maxRead * 2 <= HttpHeader.MAX_NAME_SIZE) {
                    char[] newBuffer = new char[maxRead * 2];
                    System.arraycopy(header.name, 0, newBuffer, 0, maxRead);
                    header.name = newBuffer;
                    maxRead = header.name.length;
                } else {
                    throw new IOException(sm.getString("requestStream.readline.toolong"));
                }
            }

            if (pos >= count) {
                int val = read();
                log.debug("SocketInputStream readHeader head name chr {}", chr);
                if (val == -1) {
                    throw new IOException(sm.getString("requestStream.readline.error"));
                }
                pos = 0;
                readStart = 0;
            }

            if (buf[pos] == COLON) {
                log.debug("SocketInputStream readHeader headname while End");
                colon = true;
            }
            char val = (char) buf[pos];
            if ((val >= 'A') && (val <= 'Z')) {
                val = (char) (val - LC_OFFSET);// toUp
            }

            header.name[readCount] = val;
            readCount++;
            pos++;
        }

        header.nameEnd = readCount - 1;

        // read header value(which can be spannesd over multiple lines)

        maxRead = header.value.length;
        readStart = pos;
        readCount = 0;

        int crPos = -2;// ??

        boolean eol = false;
        boolean validLine = true;

        while (validLine) {
            log.debug("SocketInputStream readHeader headvalue while Start");
            boolean space = true;
            while (space) {
                log.debug("SocketInputStream readHeader headvalue space while Start");
                if (pos >= count) {
                    int val = read();
                    log.debug("SocketInputStream readHeader head val chr {}", val);
                    if (val == -1) {
                        throw new IOException(sm.getString("requestStream.readline.error"));
                    }
                    pos = 0;
                    readStart = 0;
                }
                if ((buf[pos] == SP) || (buf[pos] == HT)) {
                    pos++;
                } else {
                    log.debug("SocketInputStream readHeader headvalue space while End");
                    space = false;
                }
            }
            while (!eol) {
                log.debug("SocketInputStream readHeader headvalue eol while Start");
                if (readCount >= maxRead) {
                    if ((2 * maxRead) <= HttpHeader.MAX_VALUE_SIZE) {
                        char[] newBuffer = new char[2 * maxRead];
                        System.arraycopy(header.value, 0, newBuffer, 0, maxRead);
                        header.value = newBuffer;
                        maxRead = header.value.length;
                    } else {
                        throw new IOException(sm.getString("requestStream.readline.toolong"));
                    }
                }
                if (pos >= count) {
                    // Copying part (or all) of the internal buffer to the
                    // line
                    // buffer
                    int val = read();
                    if (val == -1)
                        throw new IOException(sm.getString("requestStream.readline.error"));
                    pos = 0;
                    readStart = 0;
                }
                if (buf[pos] == CR) {
                } else if (buf[pos] == LF) {
                    log.debug("SocketInputStream readHeader headvalue eol while End");
                    eol = true;
                } else {
                    int ch = buf[pos] & 0xff;
                    header.value[readCount] = (char) ch;
                    readCount++;
                }
                pos++;
            }
            int nextChr = read();

            if ((nextChr != SP) && (nextChr != HT)) {
                pos--;
                log.debug("SocketInputStream readHeader headvalue while End");
                validLine = false;
            } else {
                eol = false;
                if (readCount >= maxRead) {
                    if ((2 * maxRead) <= HttpHeader.MAX_VALUE_SIZE) {
                        char[] newBuffer = new char[2 * maxRead];
                        System.arraycopy(header.value, 0, newBuffer, 0, maxRead);
                        header.value = newBuffer;
                        maxRead = header.value.length;
                    } else {
                        throw new IOException(sm.getString("requestStream.readline.toolong"));
                    }
                }
                header.value[readCount] = ' ';
                readCount++;
            }
        }

        header.valueEnd = readCount;
        log.debug("SocketInputStream readHeader End");
    }

    @Override
    public int read() throws IOException {
        if (pos >= count) {
            fill();
            if (pos >= count)
                return -1;
        }
        return buf[pos++] & 0xff;// 0xff = int -1
    }

    // Fill the internal buffer using data from the undelying input stream.
    protected void fill() throws IOException {
        pos = 0;
        count = 0;
        int nRead = inputStream.read(buf, 0, buf.length);
        if (nRead > 0) {
            count = nRead;
        }
    }

}
