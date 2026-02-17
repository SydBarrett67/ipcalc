/**
 *
 * @author alessandro.diluca
 */
public class Ipcalc {
    public static void main(String[] args) {
        String currVersion = "0.03";

        try {
            // Input validation
            if (args.length == 0) throw new Exception("Try -help / -h for command list.");
            if (args[0] == "-h" || args[0] == "-help") throw new Exception("");
            InputHandler.validate(args[0]);

            // Parsing
            String[] parts = args[0].split("/");
            IpAddress ip = new IpAddress(parts[0]);
            
            Netmask mask;
            if (parts.length < 2) {
                OutputHandler.showWarning("Netmask non specificata. Generazione in corso...");
                mask = new Netmask(ip.getDefaultPrefix());
            } else {
                mask = new Netmask(Integer.parseInt(parts[1]));
            }

            // 3. Output
            OutputHandler.printRow("Indirizzo IP", ip);
            OutputHandler.printRow("Netmask", mask);

        } catch (Exception e) {
            OutputHandler.showError(e.getMessage());
            System.exit(1);
        }

        if (args[0].equals("-version") || args[0].equals("-v")) {
            System.out.println("v" + currVersion);
            return;
        }
    }

    // Input Handler class
    public abstract class InputHandler {
        public static void isValidIP(String in) {
            if (in = null || in.isEmpty()) {
                throw new Exception("Too few arguments. Try -help / -h for command list.");
            }
            if (!input.matches("[0-9./]+")) {
                throw new Exception("Illegal characters.");
            }

            String[] parts = in.split("/");

            validateIpFormat(parts[0]);

            if (parts.length > 1) {
                validateCidrFormat(parts[1]);
            }

            if (parts.length > 2) {
                throw new Exception("Illegal IP format.");
            }
        }

        private static void validateIpFormat(String ip) throws Exception {
            String[] octets = ip.split("\\.");
            if (octets.length != 4) {
                throw new Exception("Illegal format.");
            }
        }

        private static void validateCidrFormat(String cidr) throws Exception {
            try {
                int p = Integer.parseInt(cidr);
                if (p < 0 || p > 32) throw new Exception();
            } catch (Exception e) {
                throw new Exception("Illegal CIDR format.");
            }
        }
    }

    public abstract class OutputHandler {

    }


    // Address class: conversions etc.
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