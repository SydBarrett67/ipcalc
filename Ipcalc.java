public class Ipcalc {
    public static void main(String[] args) {
        String currVersion = "0.03";

        try {
            // Controllo argomenti
            if (args.length == 0 || args[0].equals("-h") || args[0].equals("-help")) {
                System.out.println("Uso: java Ipcalc <IP>/<CIDR>");
                return;
            }

            if (args[0].equals("-v") || args[0].equals("-version")) {
                System.out.println("v" + currVersion);
                return;
            }

            // Validazione
            InputHandler.isValidIP(args[0]);

            // Parsing
            String[] parts = args[0].split("/");
            Address ip = new Address(parts[0]);
            
            Netmask mask;
            if (parts.length < 2) {
                System.out.println("Netmask non specificata. Generazione automatica...");
                mask = new Netmask(ip.getDefaultPrefix());
            } else {
                mask = new Netmask(Integer.parseInt(parts[1]));
            }

            // Output con concatenazione semplice
            System.out.println("IP Address: " + ip.getDecimalDottedQuads());
            System.out.println("Binary IP:  " + ip.getBinaryDotted());
            System.out.println("Netmask:    " + mask.getDecimalDottedQuads() + " (/" + mask.getPrefix() + ")");

        } catch (Exception e) {
            System.out.println("Errore: " + e.getMessage());
            System.exit(1);
        }
    }

    // --- CLASSI INTERNE STATICHE ---

    public static class InputHandler {
        public static void isValidIP(String in) throws Exception {
            if (!in.matches("[0-9./]+")) throw new Exception("Caratteri non validi.");
            String[] parts = in.split("/");
            
            String[] octets = parts[0].split("\\.");
            if (octets.length != 4) throw new Exception("Formato IP errato (richiesto x.x.x.x).");

            if (parts.length > 1) {
                try {
                    int p = Integer.parseInt(parts[1]);
                    if (p < 0 || p > 32) throw new Exception("CIDR fuori range (0-32).");
                } catch (NumberFormatException e) {
                    throw new Exception("CIDR deve essere un numero.");
                }
            }
        }
    }

    public static abstract class DottedQuad {
        public abstract String getDecimalDottedQuads();
        public abstract String getBinaryDotted();

        public int getDefaultPrefix() {
            // Logica delle classi di indirizzi IP (A, B, C)
            String dec = getDecimalDottedQuads();
            int firstOctet = Integer.parseInt(dec.split("\\.")[0]);

            if (firstOctet >= 1 && firstOctet <= 126) return 8;   // Classe A
            if (firstOctet >= 128 && firstOctet <= 191) return 16; // Classe B
            if (firstOctet >= 192 && firstOctet <= 223) return 24; // Classe C
            return 32;
        }
    }

    public static class Address extends DottedQuad {
        private long decIP;

        public Address(String strIP) {
            this.decIP = strToLongIP(strIP);
        }

        public Address(long decIP) {
            this.decIP = decIP & 0xFFFFFFFFL;
        }

        @Override
        public String getDecimalDottedQuads() {
            return ((decIP >> 24) & 0xFF) + "." +
                   ((decIP >> 16) & 0xFF) + "." +
                   ((decIP >> 8) & 0xFF) + "." +
                   (decIP & 0xFF);
        }

        @Override
        public String getBinaryDotted() {
            String result = "";
            for (int i = 0; i < 4; i++) {
                int octet = (int) ((decIP >> (24 - 8 * i)) & 0xFF);
                String bin = Integer.toBinaryString(octet);
                // Padding manuale senza StringBuilder
                while (bin.length() < 8) bin = "0" + bin;
                result += bin + (i < 3 ? "." : "");
            }
            return result;
        }

        private long strToLongIP(String str) {
            String[] parts = str.split("\\.");
            long res = 0;
            for (int i = 0; i < 4; i++) {
                res |= (Long.parseLong(parts[i]) << (24 - 8 * i));
            }
            return res & 0xFFFFFFFFL;
        }
    }

    public static class Netmask extends DottedQuad {
        private int prefix;
        private Address maskAddr;

        public Netmask(int prefix) {
            this.prefix = prefix;
            // Calcolo della maschera binaria
            long maskInt = (prefix == 0) ? 0 : (0xFFFFFFFFL << (32 - prefix));
            this.maskAddr = new Address(maskInt);
        }

        public int getPrefix() { return prefix; }
        public String getDecimalDottedQuads() { return maskAddr.getDecimalDottedQuads(); }
        public String getBinaryDotted() { return maskAddr.getBinaryDotted(); }
    }
}