public class ipcalc {

    public static final String RESET = "\u001B[00m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN = "\u001B[36m";

    public static final String version = "0.4";


    public static void main(String[] args) {
        if (System.getProperty("os.name").contains("Windows")) {
            try {
                new ProcessBuilder("cmd", "/c").inheritIO().start().waitFor();
            } catch (Exception e) {
                System.out.println("Colors are not supported.");
            }
        }

        try {

            if (args.length == 0) throw new Exception("-help for command list.");
        
            InputHandler.validate(args[0]);

        } catch (Exception e) {
            System.out.println(RED + "Illegal argument." + RESET);
            System.exit(1);
        }
    }

    // InputHandler: validation and processing of input
    public static class InputHandler {
        public static void validate(String in) throws Exception {

            if (in.equals("-v") || in.equals("-version")) {
                System.out.println("v"+version);
            }
            else if (in.equals("-h") || in.equals("-help")) {
                System.out.println("Command list:");
                System.out.println("-h -help : print command list");
                System.out.println("-v -version : print current version number");
                System.out.println("<IP> <NM> : calculate IP address and Netmask information");
            }
            else {
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

                Address network = new Address(computeNetworkAddress(ip, mask));
                Address broadcast = new Address(computeBroadcastAddress(ip, mask));
                Address hostMin = new Address(computeHostMin(ip, mask));
                Address hostMax = new Address(computeHostMax(ip, mask));
                double nHost = computeHostNumber(mask);

                // Printing results
                OutputHandler.showResults(ip, mask, network, broadcast, hostMin, hostMax, nHost);
            }
        }
    }

    // OutputHandler: formatting and displaying results
    public static class OutputHandler {
        public static void showResults(Address ip, Netmask mask, Address nwAddr, Address broadcast, Address hostMin, Address hostMax, double nHost) {
            System.out.println(CYAN + "--------------------------------------------------" + RESET);
            printRow("Indirizzo IP", ip, GREEN);
            printRow("Netmask", mask, GREEN);
            System.out.println(CYAN + "=>\n" + RESET);
            printRow("Network Address", nwAddr, GREEN);
            printRow("Broadcast Address", broadcast, GREEN);
            printRow("Host min", hostMin, GREEN);
            printRow("Host max", hostMax, GREEN);
            printRow("Host/Net", nHost, ip, mask, GREEN);
            System.out.println(CYAN + "--------------------------------------------------\n" + RESET);
        }

        private static void printRow(String label, DottedQuad dq, String color) {
            // String formatting with color and alignment
            System.out.format("%-15s " + color + "%-18s" + RESET + " %-15s%n", 
                label + ":", 
                dq.getDecimalDottedQuads(), 
                dq.getBinaryDotted()
            );
        }

        private static void printRow(String label, double num, DottedQuad dq, Netmask nm, String color) {
            // Class
            String classStr = "Unknown class";
            int bits = dq.getDefaultPrefix();
            switch(bits) {
                case 8:
                    classStr="A";
                    break;
                case 16:
                    classStr="B";
                    break;
                case 24:
                    classStr="C";
                    break;
            }

            // Private / Public
            String status = computeNetStatus(dq, nm);


            // String formatting with color and alignment
            System.out.format("%-15s " + color + "%-18s" + RESET + " %-15s%n", 
                label + ":", 
                num,
                "Class " + classStr + ", " + status
            );
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

        private long getDecIP() {
            return this.decIP;
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



    // NETWORK CALCULATION FUNCTIONS
    public static long computeNetworkAddress(Address ip, Netmask nm) {
        long ipBits = ip.getDecIP();
        long maskBits = new Address(nm.getDecimalDottedQuads()).getDecIP();
        
        long networkBits = ipBits & maskBits;
        
        return networkBits;
    }

    public static long computeBroadcastAddress(Address ip, Netmask nm) {
        long ipBits = ip.getDecIP();
        long maskBits = new Address(nm.getDecimalDottedQuads()).getDecIP();
        
        long invertedMask = (~maskBits) & 0xFFFFFFFFL;
        
        return ipBits | invertedMask;
    }

    public static long computeHostMin(Address ip, Netmask nm) {

        Address HostMinAddr = new Address(computeNetworkAddress(ip, nm));

        long dec = HostMinAddr.getDecIP() + 1;

        return dec;
    }

    public static long computeHostMax(Address ip, Netmask nm) {

        Address HostMaxAddr = new Address(computeBroadcastAddress(ip, nm));

        long dec = HostMaxAddr.getDecIP() - 1;

        return dec;
    }

    public static double computeHostNumber(Netmask nm) {

        int bits = 32 - nm.getPrefix();

        return Math.pow(2, bits) - 2;
    }

    public static String computeNetStatus(DottedQuad ip, Netmask nm) {
        String ipStr = ip.getDecimalDottedQuads();
        String nmStr = nm.getDecimalDottedQuads();

        String[] ipParts = ipStr.split("\\.");
        String[] nmParts = nmStr.split("\\.");
        
        long startIp = 0;
        long endIp = 0;

        for (int i = 0; i < 4; i++) {
            int ipOct = Integer.parseInt(ipParts[i]);
            int nmOct = Integer.parseInt(nmParts[i]);

            startIp = (startIp << 8) | (ipOct & nmOct);
            // Broadcast Address: IP OR (NOT Mask)
            endIp = (endIp << 8) | (ipOct | (~nmOct & 0xFF));
        }

        String[] results = new String[2];
        long[] checks = {startIp, endIp};

        for (int i = 0; i < 2; i++) {
            long addr = checks[i];
            if (addr >= 0x0A000000L && addr <= 0x0AFFFFFFL) results[i] = "private";
            else if (addr >= 0xAC100000L && addr <= 0xAC1FFFFFL) results[i] = "private";
            else if (addr >= 0xC0A80000L && addr <= 0xC0A8FFFFL) results[i] = "private";
            else if (addr >= 0x7F000000L && addr <= 0x7FFFFFFFL) results[i] = "loopback";
            else results[i] = "public";
        }

        if (results[0].equals(results[1])) {
            return results[0]; 
        } else {
            return "in part private and public";
        }
    }
}