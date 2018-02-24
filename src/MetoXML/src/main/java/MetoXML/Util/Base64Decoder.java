package MetoXML.Util;

import java.io.*;

public class Base64Decoder {
    private static final int BUFFER_SIZE = 1024;
    InputStream in = null;
    OutputStream out = null;
    boolean stringp = false;

    private void printHex(int var1) {
        int var2 = (var1 & 240) >> 4;
        int var3 = var1 & 15;
        System.out.print((new Character((char)(var2 > 9 ? 65 + var2 - 10 : 48 + var2))).toString() + (new Character((char)(var3 > 9 ? 65 + var3 - 10 : 48 + var3))).toString());
    }

    private void printHex(byte[] var1, int var2, int var3) {
        while(var2 < var3) {
            this.printHex(var1[var2++]);
            System.out.print(" ");
        }

        System.out.println("");
    }

    private void printHex(String var1) {
        byte[] var2;
        try {
            var2 = var1.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException var4) {
            throw new RuntimeException(this.getClass().getName() + "[printHex] Unable to convert" + "properly char to bytes");
        }

        this.printHex(var2, 0, var2.length);
    }

    private final int get1(byte[] var1, int var2) {
        return (var1[var2] & 63) << 2 | (var1[var2 + 1] & 48) >>> 4;
    }

    private final int get2(byte[] var1, int var2) {
        return (var1[var2 + 1] & 15) << 4 | (var1[var2 + 2] & 60) >>> 2;
    }

    private final int get3(byte[] var1, int var2) {
        return (var1[var2 + 2] & 3) << 6 | var1[var2 + 3] & 63;
    }

    private final int check(int var1) {
        if (var1 >= 65 && var1 <= 90) {
            return var1 - 65;
        } else if (var1 >= 97 && var1 <= 122) {
            return var1 - 97 + 26;
        } else if (var1 >= 48 && var1 <= 57) {
            return var1 - 48 + 52;
        } else {
            switch(var1) {
            case 43:
                return 62;
            case 47:
                return 63;
            case 61:
                return 65;
            default:
                return -1;
            }
        }
    }

    public void process() throws IOException, Base64FormatException {
        byte[] var1 = new byte[1024];
        byte[] var2 = new byte[4];
        boolean var3 = true;
        int var4 = 0;

        int var7;
        label44:
        while((var7 = this.in.read(var1)) > 0) {
            for(int var5 = 0; var5 < var7; var4 = 0) {
                while(var4 < 4) {
                    if (var5 >= var7) {
                        continue label44;
                    }

                    int var6 = this.check(var1[var5++]);
                    if (var6 >= 0) {
                        var2[var4++] = (byte)var6;
                    }
                }

                if (var2[2] == 65) {
                    this.out.write(this.get1(var2, 0));
                    return;
                }

                if (var2[3] == 65) {
                    this.out.write(this.get1(var2, 0));
                    this.out.write(this.get2(var2, 0));
                    return;
                }

                this.out.write(this.get1(var2, 0));
                this.out.write(this.get2(var2, 0));
                this.out.write(this.get3(var2, 0));
            }
        }

        if (var4 != 0) {
            throw new Base64FormatException("Invalid length.");
        } else {
            this.out.flush();
        }
    }

    public String processString() throws Base64FormatException {
        if (!this.stringp) {
            throw new RuntimeException(this.getClass().getName() + "[processString]" + "invalid call (not a String)");
        } else {
            try {
                this.process();
            } catch (IOException var4) {
                ;
            }

            try {
                String var1 = ((ByteArrayOutputStream)this.out).toString("ISO-8859-1");
                return var1;
            } catch (UnsupportedEncodingException var3) {
                throw new RuntimeException(this.getClass().getName() + "[processString] Unable to convert" + "properly char to bytes");
            }
        }
    }

    public Base64Decoder(String var1) {
        byte[] var2;
        try {
            var2 = var1.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException var4) {
            throw new RuntimeException(this.getClass().getName() + "[Constructor] Unable to convert" + "properly char to bytes");
        }

        this.stringp = true;
        this.in = new ByteArrayInputStream(var2);
        this.out = new ByteArrayOutputStream();
    }

    public Base64Decoder(InputStream var1, OutputStream var2) {
        this.in = var1;
        this.out = var2;
        this.stringp = false;
    }

    public static void main(String[] var0) {
        if (var0.length == 1) {
            try {
                Base64Decoder var1 = new Base64Decoder(var0[0]);
                System.out.println("[" + var1.processString() + "]");
            } catch (Base64FormatException var4) {
                System.out.println("Invalid Base64 format !");
                System.exit(1);
            }
        } else if (var0.length == 2 && var0[0].equals("-f")) {
            try {
                FileInputStream var5 = new FileInputStream(var0[1]);
                Base64Decoder var2 = new Base64Decoder(var5, System.out);
                var2.process();
            } catch (Exception var3) {
                System.out.println("error: " + var3.getMessage());
                System.exit(1);
            }
        } else {
            System.out.println("Base64Decoder [strong] [-f file]");
        }

        System.exit(0);
    }
}
