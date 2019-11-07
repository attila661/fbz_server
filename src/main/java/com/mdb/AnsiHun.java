package com.mdb;

import java.util.Arrays;
import java.util.stream.IntStream;

public class AnsiHun {

    static char[] b2c = new char[256];
    static byte[] b2B = new byte[256];

    static int[] ch2huOrder = new int[256];
    static int[] ch2huIcsOrder = new int[256];

    static final String hC = "áÁéÉíÍóÓöÖőŐúÚüÜűŰ";

    static final int[] hB =
        new int[]{160, 181, 130, 144, 161, 214, 162, 224, 148, 153, 139, 138, 163, 233, 129, 154, 251, 235};

    static final String hC2 = "•";
    static final int[] hB2 = new int[]{254};


    public static void init() {
        //Arrays.fill(b2c, 0, 32, '?');
        Arrays.fill(b2c, 128, 256, '?');
        IntStream.range(0, 128).forEach(i -> b2c[i] = (char) i);
        IntStream.range(0, hB.length).forEach(i -> b2c[hB[i]] = hC.charAt(i));
        IntStream.range(0, hB2.length).forEach(i -> b2c[hB2[i]] = hC2.charAt(i));

        IntStream.range(0, 256).forEach(i -> b2B[i] = (byte) i);
        IntStream.rangeClosed('a', 'z').forEach(i -> b2B[i] = (byte) (i - 'a' + 'A'));
        IntStream.range(0, hB.length).filter(i -> i % 2 == 0).forEach(i -> b2B[hB[i] & 0xFF] = (byte) hB[i + 1]);
        initCh8toHunOrder();
        initCh8toHunInCaseSensitiveOrder();

//        String h1 = "o''u''U''O''a'A'e'E'i'I'o'O'o:O:u'U'u:U:";
//        String h2 = "őűŰŐáÁéÉíÍóÓöÖúÚüÜ";
//        String h3=hun(h1);
//        System.out.println("-----------------------------------------------");
//        System.out.println(h1);
//        System.out.println(h2);
//        System.out.println(h3);
//        System.out.println(h3.equals(h2));
//        System.out.println("-----------------------------------------------");

    }

    public static String hun(String simpleHun) {
        return simpleHun
            .replaceAll("o''", "ő")
            .replaceAll("u''", "ű")
            .replaceAll("U''", "Ű")
            .replaceAll("O''", "Ő")
            .replaceAll("a'", "á")
            .replaceAll("A'", "Á")
            .replaceAll("e'", "é")
            .replaceAll("E'", "É")
            .replaceAll("i'", "í")
            .replaceAll("I'", "Í")
            .replaceAll("o'", "ó")
            .replaceAll("O'", "Ó")
            .replaceAll("o:", "ö")
            .replaceAll("O:", "Ö")
            .replaceAll("u'", "ú")
            .replaceAll("U'", "Ú")
            .replaceAll("u:", "ü")
            .replaceAll("U:", "Ü");
    }

    public static byte[] string2byteArrayU(String pattern) throws Exception {
        if (pattern == null || pattern.length() == 0) {
            return new byte[0];
        }
        int l = pattern.length();
        byte[] out = new byte[l];
        for (int i = 0; i < l; i++) {
            char c = pattern.charAt(i);
            if (c > 255) {
                int h = hC.indexOf(c);
                if (c < 0) {
                    throw new Exception("Invalid pattern");
                }
                c = (char) hB[h];
            }
            out[i] = b2B[c];
        }
        return out;
    }

    public static String ba2s(byte[] ba) {
        return ba2s(ba, 0, ba.length);
    }

    public static String ba2s(byte[] ba, int offset, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(b2c[ba[i + offset] & 0xFF]);
        }
        return sb.toString();
    }

    public static void toUpperInplace(byte[] b) {
        for (int i = 0; i < b.length; i++) {
            b[i] = b2B[b[i] & 0xFF];
        }
    }

    public static void qsFindAll(byte[] qsdata, int recLen, byte[] pattern, boolean[] result) {
        if (pattern.length > recLen) {
            return;
        }
        int cnt = qsdata.length / recLen;
        int pos;
        int r = 0;
        while ((pos = indexOf(qsdata, pattern, r * recLen)) >= 0) {
            r = pos / recLen;
            result[r++] = true;
            if (r >= cnt) {
                return;
            }
        }
    }

    private static int indexOf(byte[] data, byte[] pattern, int offset) {
        int pl = pattern.length;
        if (pl == 0) {
            return offset;
        }
        int m = data.length - pl;
        int j;
        for (int i = offset; i < m; i++) {
            for (j = 0; j < pl; j++) {
                if (data[i + j] != pattern[j]) {
                    break;
                }
            }
            if (j == pl) {
                return i;
            }
        }
        return -1;
    }

    private static void initCh8toHunInCaseSensitiveOrder() {
        ch2huIcsOrder[0] = 0;
        ch2huIcsOrder[1] = 1;
        ch2huIcsOrder[2] = 2;
        ch2huIcsOrder[3] = 3;
        ch2huIcsOrder[4] = 4;
        ch2huIcsOrder[5] = 5;
        ch2huIcsOrder[6] = 6;
        ch2huIcsOrder[7] = 7;
        ch2huIcsOrder[8] = 8;
        ch2huIcsOrder[9] = 9;
        ch2huIcsOrder[10] = 10;
        ch2huIcsOrder[11] = 11;
        ch2huIcsOrder[12] = 12;
        ch2huIcsOrder[13] = 13;
        ch2huIcsOrder[14] = 14;
        ch2huIcsOrder[15] = 15;
        ch2huIcsOrder[16] = 16;
        ch2huIcsOrder[17] = 17;
        ch2huIcsOrder[18] = 18;
        ch2huIcsOrder[19] = 19;
        ch2huIcsOrder[20] = 20;
        ch2huIcsOrder[21] = 21;
        ch2huIcsOrder[22] = 22;
        ch2huIcsOrder[23] = 23;
        ch2huIcsOrder[24] = 24;
        ch2huIcsOrder[25] = 25;
        ch2huIcsOrder[26] = 26;
        ch2huIcsOrder[27] = 27;
        ch2huIcsOrder[28] = 28;
        ch2huIcsOrder[29] = 29;
        ch2huIcsOrder[30] = 30;
        ch2huIcsOrder[31] = 31;
        ch2huIcsOrder[32] = 32;
        ch2huIcsOrder[33] = 33;
        ch2huIcsOrder[34] = 34;
        ch2huIcsOrder[35] = 35;
        ch2huIcsOrder[36] = 36;
        ch2huIcsOrder[37] = 37;
        ch2huIcsOrder[38] = 38;
        ch2huIcsOrder[39] = 39;
        ch2huIcsOrder[40] = 40;
        ch2huIcsOrder[41] = 41;
        ch2huIcsOrder[42] = 42;
        ch2huIcsOrder[43] = 43;
        ch2huIcsOrder[44] = 44;
        ch2huIcsOrder[45] = 45;
        ch2huIcsOrder[46] = 46;
        ch2huIcsOrder[47] = 47;
        ch2huIcsOrder[48] = 48;
        ch2huIcsOrder[49] = 49;
        ch2huIcsOrder[50] = 50;
        ch2huIcsOrder[51] = 51;
        ch2huIcsOrder[52] = 52;
        ch2huIcsOrder[53] = 53;
        ch2huIcsOrder[54] = 54;
        ch2huIcsOrder[55] = 55;
        ch2huIcsOrder[56] = 56;
        ch2huIcsOrder[57] = 57;
        ch2huIcsOrder[58] = 58;
        ch2huIcsOrder[59] = 59;
        ch2huIcsOrder[60] = 60;
        ch2huIcsOrder[61] = 61;
        ch2huIcsOrder[62] = 62;
        ch2huIcsOrder[63] = 63;
        ch2huIcsOrder[64] = 64;
        ch2huIcsOrder[97] = 65;
        ch2huIcsOrder[65] = 65;
        ch2huIcsOrder[160] = 66;
        ch2huIcsOrder[181] = 66;
        ch2huIcsOrder[131] = 67;
        ch2huIcsOrder[182] = 67;
        ch2huIcsOrder[132] = 68;
        ch2huIcsOrder[142] = 68;
        ch2huIcsOrder[199] = 69;
        ch2huIcsOrder[198] = 69;
        ch2huIcsOrder[165] = 70;
        ch2huIcsOrder[164] = 70;
        ch2huIcsOrder[98] = 71;
        ch2huIcsOrder[66] = 71;
        ch2huIcsOrder[99] = 72;
        ch2huIcsOrder[67] = 72;
        ch2huIcsOrder[135] = 73;
        ch2huIcsOrder[128] = 73;
        ch2huIcsOrder[134] = 74;
        ch2huIcsOrder[143] = 74;
        ch2huIcsOrder[159] = 75;
        ch2huIcsOrder[172] = 75;
        ch2huIcsOrder[100] = 76;
        ch2huIcsOrder[68] = 76;
        ch2huIcsOrder[208] = 77;
        ch2huIcsOrder[209] = 77;
        ch2huIcsOrder[212] = 78;
        ch2huIcsOrder[210] = 78;
        ch2huIcsOrder[101] = 79;
        ch2huIcsOrder[69] = 79;
        ch2huIcsOrder[130] = 80;
        ch2huIcsOrder[144] = 80;
        ch2huIcsOrder[137] = 81;
        ch2huIcsOrder[211] = 81;
        ch2huIcsOrder[216] = 82;
        ch2huIcsOrder[183] = 82;
        ch2huIcsOrder[169] = 83;
        ch2huIcsOrder[168] = 83;
        ch2huIcsOrder[102] = 84;
        ch2huIcsOrder[70] = 84;
        ch2huIcsOrder[103] = 85;
        ch2huIcsOrder[71] = 85;
        ch2huIcsOrder[104] = 86;
        ch2huIcsOrder[72] = 86;
        ch2huIcsOrder[105] = 87;
        ch2huIcsOrder[73] = 87;
        ch2huIcsOrder[161] = 88;
        ch2huIcsOrder[214] = 88;
        ch2huIcsOrder[140] = 89;
        ch2huIcsOrder[215] = 89;
        ch2huIcsOrder[106] = 90;
        ch2huIcsOrder[74] = 90;
        ch2huIcsOrder[107] = 91;
        ch2huIcsOrder[75] = 91;
        ch2huIcsOrder[108] = 92;
        ch2huIcsOrder[76] = 92;
        ch2huIcsOrder[146] = 93;
        ch2huIcsOrder[145] = 93;
        ch2huIcsOrder[150] = 94;
        ch2huIcsOrder[149] = 94;
        ch2huIcsOrder[136] = 95;
        ch2huIcsOrder[157] = 95;
        ch2huIcsOrder[109] = 96;
        ch2huIcsOrder[77] = 96;
        ch2huIcsOrder[110] = 97;
        ch2huIcsOrder[78] = 97;
        ch2huIcsOrder[228] = 98;
        ch2huIcsOrder[227] = 98;
        ch2huIcsOrder[229] = 99;
        ch2huIcsOrder[213] = 99;
        ch2huIcsOrder[111] = 100;
        ch2huIcsOrder[79] = 100;
        ch2huIcsOrder[162] = 101;
        ch2huIcsOrder[224] = 101;
        ch2huIcsOrder[148] = 102;
        ch2huIcsOrder[153] = 102;
        ch2huIcsOrder[139] = 103;
        ch2huIcsOrder[138] = 103;
        ch2huIcsOrder[147] = 104;
        ch2huIcsOrder[226] = 104;
        ch2huIcsOrder[112] = 105;
        ch2huIcsOrder[80] = 105;
        ch2huIcsOrder[113] = 106;
        ch2huIcsOrder[81] = 106;
        ch2huIcsOrder[114] = 107;
        ch2huIcsOrder[82] = 107;
        ch2huIcsOrder[253] = 108;
        ch2huIcsOrder[252] = 108;
        ch2huIcsOrder[234] = 109;
        ch2huIcsOrder[232] = 109;
        ch2huIcsOrder[115] = 110;
        ch2huIcsOrder[83] = 110;
        ch2huIcsOrder[152] = 111;
        ch2huIcsOrder[151] = 111;
        ch2huIcsOrder[231] = 112;
        ch2huIcsOrder[230] = 112;
        ch2huIcsOrder[173] = 113;
        ch2huIcsOrder[184] = 113;
        ch2huIcsOrder[116] = 114;
        ch2huIcsOrder[84] = 114;
        ch2huIcsOrder[238] = 115;
        ch2huIcsOrder[221] = 115;
        ch2huIcsOrder[156] = 116;
        ch2huIcsOrder[155] = 116;
        ch2huIcsOrder[117] = 117;
        ch2huIcsOrder[85] = 117;
        ch2huIcsOrder[163] = 118;
        ch2huIcsOrder[233] = 118;
        ch2huIcsOrder[129] = 119;
        ch2huIcsOrder[154] = 119;
        ch2huIcsOrder[251] = 120;
        ch2huIcsOrder[235] = 120;
        ch2huIcsOrder[133] = 121;
        ch2huIcsOrder[222] = 121;
        ch2huIcsOrder[118] = 122;
        ch2huIcsOrder[86] = 122;
        ch2huIcsOrder[119] = 123;
        ch2huIcsOrder[87] = 123;
        ch2huIcsOrder[120] = 124;
        ch2huIcsOrder[88] = 124;
        ch2huIcsOrder[121] = 125;
        ch2huIcsOrder[89] = 125;
        ch2huIcsOrder[236] = 126;
        ch2huIcsOrder[237] = 126;
        ch2huIcsOrder[122] = 127;
        ch2huIcsOrder[90] = 127;
        ch2huIcsOrder[171] = 128;
        ch2huIcsOrder[141] = 128;
        ch2huIcsOrder[190] = 129;
        ch2huIcsOrder[189] = 129;
        ch2huIcsOrder[167] = 130;
        ch2huIcsOrder[166] = 130;
        ch2huIcsOrder[91] = 131;
        ch2huIcsOrder[92] = 131;
        ch2huIcsOrder[93] = 132;
        ch2huIcsOrder[94] = 133;
        ch2huIcsOrder[95] = 134;
        ch2huIcsOrder[96] = 135;
        ch2huIcsOrder[123] = 136;
        ch2huIcsOrder[124] = 137;
        ch2huIcsOrder[125] = 138;
        ch2huIcsOrder[126] = 139;
        ch2huIcsOrder[127] = 140;
        ch2huIcsOrder[158] = 141;
        ch2huIcsOrder[170] = 142;
        ch2huIcsOrder[174] = 143;
        ch2huIcsOrder[175] = 144;
        ch2huIcsOrder[176] = 145;
        ch2huIcsOrder[177] = 146;
        ch2huIcsOrder[178] = 147;
        ch2huIcsOrder[179] = 148;
        ch2huIcsOrder[180] = 149;
        ch2huIcsOrder[185] = 150;
        ch2huIcsOrder[186] = 151;
        ch2huIcsOrder[187] = 152;
        ch2huIcsOrder[188] = 153;
        ch2huIcsOrder[191] = 154;
        ch2huIcsOrder[192] = 155;
        ch2huIcsOrder[193] = 156;
        ch2huIcsOrder[194] = 157;
        ch2huIcsOrder[195] = 158;
        ch2huIcsOrder[196] = 159;
        ch2huIcsOrder[197] = 160;
        ch2huIcsOrder[200] = 161;
        ch2huIcsOrder[201] = 162;
        ch2huIcsOrder[202] = 163;
        ch2huIcsOrder[203] = 164;
        ch2huIcsOrder[204] = 165;
        ch2huIcsOrder[205] = 166;
        ch2huIcsOrder[206] = 167;
        ch2huIcsOrder[207] = 168;
        ch2huIcsOrder[217] = 169;
        ch2huIcsOrder[218] = 170;
        ch2huIcsOrder[219] = 171;
        ch2huIcsOrder[220] = 172;
        ch2huIcsOrder[223] = 173;
        ch2huIcsOrder[225] = 174;
        ch2huIcsOrder[239] = 175;
        ch2huIcsOrder[240] = 176;
        ch2huIcsOrder[241] = 177;
        ch2huIcsOrder[242] = 178;
        ch2huIcsOrder[243] = 179;
        ch2huIcsOrder[244] = 180;
        ch2huIcsOrder[245] = 181;
        ch2huIcsOrder[246] = 182;
        ch2huIcsOrder[247] = 183;
        ch2huIcsOrder[248] = 184;
        ch2huIcsOrder[249] = 185;
        ch2huIcsOrder[250] = 186;
        ch2huIcsOrder[254] = 187;
        ch2huIcsOrder[255] = 188;
    }

    private static void initCh8toHunOrder() {
        ch2huOrder[0] = 0;
        ch2huOrder[1] = 1;
        ch2huOrder[2] = 2;
        ch2huOrder[3] = 3;
        ch2huOrder[4] = 4;
        ch2huOrder[5] = 5;
        ch2huOrder[6] = 6;
        ch2huOrder[7] = 7;
        ch2huOrder[8] = 8;
        ch2huOrder[9] = 9;
        ch2huOrder[10] = 10;
        ch2huOrder[11] = 11;
        ch2huOrder[12] = 12;
        ch2huOrder[13] = 13;
        ch2huOrder[14] = 14;
        ch2huOrder[15] = 15;
        ch2huOrder[16] = 16;
        ch2huOrder[17] = 17;
        ch2huOrder[18] = 18;
        ch2huOrder[19] = 19;
        ch2huOrder[20] = 20;
        ch2huOrder[21] = 21;
        ch2huOrder[22] = 22;
        ch2huOrder[23] = 23;
        ch2huOrder[24] = 24;
        ch2huOrder[25] = 25;
        ch2huOrder[26] = 26;
        ch2huOrder[27] = 27;
        ch2huOrder[28] = 28;
        ch2huOrder[29] = 29;
        ch2huOrder[30] = 30;
        ch2huOrder[31] = 31;
        ch2huOrder[32] = 32;
        ch2huOrder[33] = 33;
        ch2huOrder[34] = 34;
        ch2huOrder[35] = 35;
        ch2huOrder[36] = 36;
        ch2huOrder[37] = 37;
        ch2huOrder[38] = 38;
        ch2huOrder[39] = 39;
        ch2huOrder[40] = 40;
        ch2huOrder[41] = 41;
        ch2huOrder[42] = 42;
        ch2huOrder[43] = 43;
        ch2huOrder[44] = 44;
        ch2huOrder[45] = 45;
        ch2huOrder[46] = 46;
        ch2huOrder[47] = 47;
        ch2huOrder[48] = 48;
        ch2huOrder[49] = 49;
        ch2huOrder[50] = 50;
        ch2huOrder[51] = 51;
        ch2huOrder[52] = 52;
        ch2huOrder[53] = 53;
        ch2huOrder[54] = 54;
        ch2huOrder[55] = 55;
        ch2huOrder[56] = 56;
        ch2huOrder[57] = 57;
        ch2huOrder[58] = 58;
        ch2huOrder[59] = 59;
        ch2huOrder[60] = 60;
        ch2huOrder[61] = 61;
        ch2huOrder[62] = 62;
        ch2huOrder[63] = 63;
        ch2huOrder[64] = 64;
        ch2huOrder[97] = 65;
        ch2huOrder[65] = 66;
        ch2huOrder[160] = 67;
        ch2huOrder[181] = 68;
        ch2huOrder[131] = 69;
        ch2huOrder[182] = 70;
        ch2huOrder[132] = 71;
        ch2huOrder[142] = 72;
        ch2huOrder[199] = 73;
        ch2huOrder[198] = 74;
        ch2huOrder[165] = 75;
        ch2huOrder[164] = 76;
        ch2huOrder[98] = 77;
        ch2huOrder[66] = 78;
        ch2huOrder[99] = 79;
        ch2huOrder[67] = 80;
        ch2huOrder[135] = 81;
        ch2huOrder[128] = 82;
        ch2huOrder[134] = 83;
        ch2huOrder[143] = 84;
        ch2huOrder[159] = 85;
        ch2huOrder[172] = 86;
        ch2huOrder[100] = 87;
        ch2huOrder[68] = 88;
        ch2huOrder[208] = 89;
        ch2huOrder[209] = 90;
        ch2huOrder[212] = 91;
        ch2huOrder[210] = 92;
        ch2huOrder[101] = 93;
        ch2huOrder[69] = 94;
        ch2huOrder[130] = 95;
        ch2huOrder[144] = 96;
        ch2huOrder[137] = 97;
        ch2huOrder[211] = 98;
        ch2huOrder[216] = 99;
        ch2huOrder[183] = 100;
        ch2huOrder[169] = 101;
        ch2huOrder[168] = 102;
        ch2huOrder[102] = 103;
        ch2huOrder[70] = 104;
        ch2huOrder[103] = 105;
        ch2huOrder[71] = 106;
        ch2huOrder[104] = 107;
        ch2huOrder[72] = 108;
        ch2huOrder[105] = 109;
        ch2huOrder[73] = 110;
        ch2huOrder[161] = 111;
        ch2huOrder[214] = 112;
        ch2huOrder[140] = 113;
        ch2huOrder[215] = 114;
        ch2huOrder[106] = 115;
        ch2huOrder[74] = 116;
        ch2huOrder[107] = 117;
        ch2huOrder[75] = 118;
        ch2huOrder[108] = 119;
        ch2huOrder[76] = 120;
        ch2huOrder[146] = 121;
        ch2huOrder[145] = 122;
        ch2huOrder[150] = 123;
        ch2huOrder[149] = 124;
        ch2huOrder[136] = 125;
        ch2huOrder[157] = 126;
        ch2huOrder[109] = 127;
        ch2huOrder[77] = 128;
        ch2huOrder[110] = 129;
        ch2huOrder[78] = 130;
        ch2huOrder[228] = 131;
        ch2huOrder[227] = 132;
        ch2huOrder[229] = 133;
        ch2huOrder[213] = 134;
        ch2huOrder[111] = 135;
        ch2huOrder[79] = 136;
        ch2huOrder[162] = 137;
        ch2huOrder[224] = 138;
        ch2huOrder[148] = 139;
        ch2huOrder[153] = 140;
        ch2huOrder[139] = 141;
        ch2huOrder[138] = 142;
        ch2huOrder[147] = 143;
        ch2huOrder[226] = 144;
        ch2huOrder[112] = 145;
        ch2huOrder[80] = 146;
        ch2huOrder[113] = 147;
        ch2huOrder[81] = 148;
        ch2huOrder[114] = 149;
        ch2huOrder[82] = 150;
        ch2huOrder[253] = 151;
        ch2huOrder[252] = 152;
        ch2huOrder[234] = 153;
        ch2huOrder[232] = 154;
        ch2huOrder[115] = 155;
        ch2huOrder[83] = 156;
        ch2huOrder[152] = 157;
        ch2huOrder[151] = 158;
        ch2huOrder[231] = 159;
        ch2huOrder[230] = 160;
        ch2huOrder[173] = 161;
        ch2huOrder[184] = 162;
        ch2huOrder[116] = 163;
        ch2huOrder[84] = 164;
        ch2huOrder[238] = 165;
        ch2huOrder[221] = 166;
        ch2huOrder[156] = 167;
        ch2huOrder[155] = 168;
        ch2huOrder[117] = 169;
        ch2huOrder[85] = 170;
        ch2huOrder[163] = 171;
        ch2huOrder[233] = 172;
        ch2huOrder[129] = 173;
        ch2huOrder[154] = 174;
        ch2huOrder[251] = 175;
        ch2huOrder[235] = 176;
        ch2huOrder[133] = 177;
        ch2huOrder[222] = 178;
        ch2huOrder[118] = 179;
        ch2huOrder[86] = 180;
        ch2huOrder[119] = 181;
        ch2huOrder[87] = 182;
        ch2huOrder[120] = 183;
        ch2huOrder[88] = 184;
        ch2huOrder[121] = 185;
        ch2huOrder[89] = 186;
        ch2huOrder[236] = 187;
        ch2huOrder[237] = 188;
        ch2huOrder[122] = 189;
        ch2huOrder[90] = 190;
        ch2huOrder[171] = 191;
        ch2huOrder[141] = 192;
        ch2huOrder[190] = 193;
        ch2huOrder[189] = 194;
        ch2huOrder[167] = 195;
        ch2huOrder[166] = 196;
        ch2huOrder[91] = 197;
        ch2huOrder[92] = 198;
        ch2huOrder[93] = 199;
        ch2huOrder[94] = 200;
        ch2huOrder[95] = 201;
        ch2huOrder[96] = 202;
        ch2huOrder[123] = 203;
        ch2huOrder[124] = 204;
        ch2huOrder[125] = 205;
        ch2huOrder[126] = 206;
        ch2huOrder[127] = 207;
        ch2huOrder[158] = 208;
        ch2huOrder[170] = 209;
        ch2huOrder[174] = 210;
        ch2huOrder[175] = 211;
        ch2huOrder[176] = 212;
        ch2huOrder[177] = 213;
        ch2huOrder[178] = 214;
        ch2huOrder[179] = 215;
        ch2huOrder[180] = 216;
        ch2huOrder[185] = 217;
        ch2huOrder[186] = 218;
        ch2huOrder[187] = 219;
        ch2huOrder[188] = 220;
        ch2huOrder[191] = 221;
        ch2huOrder[192] = 222;
        ch2huOrder[193] = 223;
        ch2huOrder[194] = 224;
        ch2huOrder[195] = 225;
        ch2huOrder[196] = 226;
        ch2huOrder[197] = 227;
        ch2huOrder[200] = 228;
        ch2huOrder[201] = 229;
        ch2huOrder[202] = 230;
        ch2huOrder[203] = 231;
        ch2huOrder[204] = 232;
        ch2huOrder[205] = 233;
        ch2huOrder[206] = 234;
        ch2huOrder[207] = 235;
        ch2huOrder[217] = 236;
        ch2huOrder[218] = 237;
        ch2huOrder[219] = 238;
        ch2huOrder[220] = 239;
        ch2huOrder[223] = 240;
        ch2huOrder[225] = 241;
        ch2huOrder[239] = 242;
        ch2huOrder[240] = 243;
        ch2huOrder[241] = 244;
        ch2huOrder[242] = 245;
        ch2huOrder[243] = 246;
        ch2huOrder[244] = 247;
        ch2huOrder[245] = 248;
        ch2huOrder[246] = 249;
        ch2huOrder[247] = 250;
        ch2huOrder[248] = 251;
        ch2huOrder[249] = 252;
        ch2huOrder[250] = 253;
        ch2huOrder[254] = 254;
        ch2huOrder[255] = 255;
    }

}
