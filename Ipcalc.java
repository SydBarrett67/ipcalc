package ipcalc;

/**
 *
 * @author alessandro.diluca
 */
public class Ipcalc {
    public static void main(String[] args) {
        String currVersion = "0.03";

        if (args.length == 0) {
            System.out.println("Usa -help per la lista comandi");
            return;
        }

        if (args.length > 2) {
            System.out.println("Troppi argomenti.");
            return;
        }

        if (args[0].equals("-help")) {
            System.out.println("Command list:"
                    + "\n-help: command list"
                    + "\n-version: show current version"
                    + "\n-ipcalc <ip>: calculate IP");
            return;
        }

        if (args[0].equals("-version")) {
            System.out.println("v" + currVersion);
            return;
        }

        if (args[0].equals("-ipcalc") && args.length == 2) {
            Address ip = new Address(args[1]);
            System.out.println("Decimal dotted IP: "+ip.getDecimalDottedQuads());
            System.out.println("Integer IP (long): "+ip.getIntIP());
            System.out.println("Binary dotted IP: "+ip.getBinaryDotted());
        }
    }

    
    public static class Address {

        private String strIP;
        private long decIP;
        private String binIP;

        public Address(String strIP) {
            this.decIP = strToLongIP(strIP);
            this.strIP = longToStrIP(this.decIP);
            this.binIP = longToBinDotted(this.decIP);
        }

        public Address(int decIP) {
            this.decIP = decIP & 0xFFFFFFFFL;
            this.strIP = longToStrIP(this.decIP);
            this.binIP = longToBinDotted(this.decIP);
        }


        // Get
        public long getIntIP() {
            return decIP;
        }

        public String getDecimalDottedQuads() {
            return strIP;
        }

        public String getBinaryDotted() {
            return binIP;
        }

        // Conversioni
        private long strToLongIP(String str) {
            if (str == null) return 0;

            String[] parts = str.split("\\.");
            if (parts.length != 4) return 0;

            long result = 0;

            for (int i = 0; i < 4; i++) {
                String p = parts[i];
                if (p.length() == 0 || p.length() > 3) return 0;

                int octet = 0;
                for (int j = 0; j < p.length(); j++) {
                    char c = p.charAt(j);
                    if (c < '0' || c > '9') return 0;
                    octet = octet * 10 + (c - '0');
                }

                if (octet > 255) return 0;

                result |= (long) octet << (24 - 8 * i);
            }

            return result;
        }


        private String longToStrIP(long ip) {
            String result = "";
            for (int i = 0; i < 4; i++) {
                result += (ip >> (24 - 8 * i)) & 0xFF;
                if (i < 3) result += ".";
            }
            return result;
        }

        private String longToBinDotted(long ip) {
            String result = "";
            for (int i = 0; i < 4; i++) {
                int octet = (int) ((ip >> (24 - 8 * i)) & 0xFF);
                String bin = Integer.toBinaryString(octet);
                while (bin.length() < 8) bin = "0" + bin;
                result += bin;
                if (i < 3) result += ".";
            }
            return result;
        }
    }

}