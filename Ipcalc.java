public class ipcalc {

    public static void main(String[] args) {
        try {
            if (args.length == 0) throw new Exception("Usage: java ipcalc <IP>/<CIDR>");
        
            InputHandler.validate(args[0]);

        } catch (Exception e) {
            System.out.println("Error: " + (e.getMessage().isEmpty() ? "Unknown error" : e.getMessage()));
            System.exit(1);
        }
    }

    // InputHandler: validation and processing of input
    public static class InputHandler {
        public static void validate(String in) throws Exception {
            // Validazione preliminare
            if (!in.matches("[0-9./]+")) throw new Exception("Illegal characters.");

            String[] parts = in.split("/");
            Address ip = new Address(parts[0]);
            
            // CIDR class to handle CIDR parsing and validation
            Cidr cidr = new Cidr(parts.length > 1 ? parts[1] : null);
            Netmask mask;

            if (!cidr.isSet()) {
                System.out.println("Unspecified Netmask. Generating...");
                mask = new Netmask(ip.getDefaultPrefix());
            } else {
                mask = new Netmask(cidr.getValue());
            }

            // Printing results...
            OutputHandler.showResults(ip, mask);
        }
    }

    // OutputHandler: formatting and displaying results
    public static class OutputHandler {
        public static void showResults(Address ip, Netmask mask) {
            printRow("IP Address", ip);
            printRow("Netmask", mask);
        }

        private static void printRow(String label, DottedQuad dq) {
            System.out.format("%-15s %-18s %-15s%n", 
                label + ":", 
                dq.getDecimalDottedQuads(), 
                dq.getBinaryDotted());
        }
    }

    // CIDR class to handle CIDR parsing and validation
    public static class Cidr {
        private int value;
        private boolean isSet = false;

        public Cidr(String cidrStr) throws Exception {
            if (cidrStr != null && !cidrStr.isEmpty()) {
                try {
                    this.value = Integer.parseInt(cidrStr);
                    if (this.value < 0 || this.value > 32) throw new Exception("CIDR prefix out of bounds (0-32).");
                    this.isSet = true;
                } catch (NumberFormatException e) {
                    throw new Exception("CIDR prefix must be a number.");
                }
            }
        }
        public boolean isSet() { return isSet; }
        public int getValue() { return value; }
    }

    // DottedQuad, parent class to Address and Netmask
    public static abstract class DottedQuad {
        public abstract String getDecimalDottedQuads();
        public abstract String getBinaryDotted();

        // Default Netmask prefix based on IP class
        public int getDefaultPrefix() {
            int firstOctet = Integer.parseInt(getDecimalDottedQuads().split("\\.")[0]);
            if (firstOctet >= 1 && firstOctet <= 126) return 8;    // Class A
            if (firstOctet >= 128 && firstOctet <= 191) return 16; // Class B
            if (firstOctet >= 192 && firstOctet <= 223) return 24; // Class C
            return 32;
        }
    }

    public static class Address extends DottedQuad {
        private long decIP;

        // Costructors
        public Address(String strIP) { this.decIP = strToLongIP(strIP); }
        public Address(long decIP) { this.decIP = decIP & 0xFFFFFFFFL; }

        // Getters
        public String getDecimalDottedQuads() {
            return ((decIP >> 24) & 0xFF) + "." + ((decIP >> 16) & 0xFF) + "." +
                   ((decIP >> 8) & 0xFF) + "." + (decIP & 0xFF);
        }

        public String getBinaryDotted() {
            String res = "";
            for (int i = 0; i < 4; i++) {
                String bin = Integer.toBinaryString((int)(decIP >> (24 - 8 * i)) & 0xFF);
                while (bin.length() < 8) bin = "0" + bin;
                res += bin + (i < 3 ? "." : "");
            }
            return res;
        }


        // Conversion from string IP to long integer
        private long strToLongIP(String str) {
            String[] parts = str.split("\\.");
            long res = 0;
            for (int i = 0; i < 4; i++) res |= (Long.parseLong(parts[i]) << (24 - 8 * i));
            return res & 0xFFFFFFFFL;
        }
    }

    // Netmask class given a CIDR prefix.
    public static class Netmask extends DottedQuad {
        private int prefix;
        private Address maskAddr;

        public Netmask(int prefix) {
            this.prefix = prefix;
            long maskInt = (prefix == 0) ? 0 : (0xFFFFFFFFL << (32 - prefix));
            this.maskAddr = new Address(maskInt);
        }
        // Getters
        public int getPrefix() { return prefix; }
        public String getDecimalDottedQuads() { return maskAddr.getDecimalDottedQuads(); }
        public String getBinaryDotted() { return maskAddr.getBinaryDotted(); }
    }
}