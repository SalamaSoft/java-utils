package MetoXML.Util;

import java.io.*;

public class Base64Encoder {
    private static final int BUFFER_SIZE = 1024;
    private static byte[] encoding = new byte[]{65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 43, 47, 61};
    InputStream in = null;
    OutputStream out = null;
    boolean stringp = false;

    private final int get1(byte[] var1, int var2) {
        return (var1[var2] & 252) >> 2;
    }

    private final int get2(byte[] var1, int var2) {
        return (var1[var2] & 3) << 4 | (var1[var2 + 1] & 240) >>> 4;
    }

    private final int get3(byte[] var1, int var2) {
        return (var1[var2 + 1] & 15) << 2 | (var1[var2 + 2] & 192) >>> 6;
    }

    private static final int get4(byte[] var0, int var1) {
        return var0[var1 + 2] & 63;
    }

    public void process() throws IOException {
        byte[] var1 = new byte[1024];
        boolean var2 = true;
        int var3 = 0;
        int var4 = 0;

        while(true) {
            int var9;
            while((var9 = this.in.read(var1, var3, 1024 - var3)) > 0) {
                if (var9 + var3 >= 3) {
                    var9 += var3;

                    int var5;
                    for(var3 = 0; var3 + 3 <= var9; var3 += 3) {
                        var5 = this.get1(var1, var3);
                        int var6 = this.get2(var1, var3);
                        int var7 = this.get3(var1, var3);
                        int var8 = get4(var1, var3);
                        switch(var4) {
                        case 73:
                            this.out.write(encoding[var5]);
                            this.out.write(encoding[var6]);
                            this.out.write(encoding[var7]);
                            this.out.write(10);
                            this.out.write(encoding[var8]);
                            var4 = 1;
                            break;
                        case 74:
                            this.out.write(encoding[var5]);
                            this.out.write(encoding[var6]);
                            this.out.write(10);
                            this.out.write(encoding[var7]);
                            this.out.write(encoding[var8]);
                            var4 = 2;
                            break;
                        case 75:
                            this.out.write(encoding[var5]);
                            this.out.write(10);
                            this.out.write(encoding[var6]);
                            this.out.write(encoding[var7]);
                            this.out.write(encoding[var8]);
                            var4 = 3;
                            break;
                        case 76:
                            this.out.write(10);
                            this.out.write(encoding[var5]);
                            this.out.write(encoding[var6]);
                            this.out.write(encoding[var7]);
                            this.out.write(encoding[var8]);
                            var4 = 4;
                            break;
                        default:
                            this.out.write(encoding[var5]);
                            this.out.write(encoding[var6]);
                            this.out.write(encoding[var7]);
                            this.out.write(encoding[var8]);
                            var4 += 4;
                        }
                    }

                    for(var5 = 0; var5 < 3; ++var5) {
                        var1[var5] = var5 < var9 - var3 ? var1[var3 + var5] : 0;
                    }

                    var3 = var9 - var3;
                } else {
                    var3 += var9;
                }
            }

            switch(var3) {
            case 1:
                this.out.write(encoding[this.get1(var1, 0)]);
                this.out.write(encoding[this.get2(var1, 0)]);
                this.out.write(61);
                this.out.write(61);
                break;
            case 2:
                this.out.write(encoding[this.get1(var1, 0)]);
                this.out.write(encoding[this.get2(var1, 0)]);
                this.out.write(encoding[this.get3(var1, 0)]);
                this.out.write(61);
            }

            return;
        }
    }

    public String processString() {
        if (!this.stringp) {
            throw new RuntimeException(this.getClass().getName() + "[processString]" + "invalid call (not a String)");
        } else {
            try {
                this.process();
            } catch (IOException var2) {
                ;
            }

            return ((ByteArrayOutputStream)this.out).toString();
        }
    }

    public Base64Encoder(String var1) {
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

    public Base64Encoder(InputStream var1, OutputStream var2) {
        this.in = var1;
        this.out = var2;
        this.stringp = false;
    }

    public static void main(String[] var0) {
        if (var0.length != 1) {
            System.out.println("Base64Encoder <string>");
            System.exit(0);
        }

        Base64Encoder var1 = new Base64Encoder(var0[0]);
        System.out.println("[" + var1.processString() + "]");
    }
}
