import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main {
    private static final String[] METHODS = new String[]{AmmoFactory.PUT, AmmoFactory.GET, AmmoFactory.MIX};

    // Requests amount
    private final static String SPLITTED_MODE = "split";
    private final static String COMBINED_MODE = "combine";
    private final static int MIN_ARGS_LENGTH = 7;

    private static class ReqConfig {
        private final static Set<String> METHODS = new HashSet<>(Arrays.asList(Main.METHODS));

        private final static String PREFIX_SPLITTER = "_";
        private final static String SLASH = "/";
        private final static String REPLICAS = "replicas";

        private final String method;
        private final String uri;
        private final int amount;
        private final String file;
        private String[] factors;
        private int offset = 0;

        public ReqConfig(String method, String uri, int amount, String file, String[] factors, int offset) {
            this.method = method;
            this.uri = uri;
            this.amount = amount;
            this.file = file;
            this.factors = factors;
            this.offset = offset;
        }

        public static ReqConfig generate(final String[] args, int start) {
            int i = start;
            String method, uri, file;
            int amount;
            ArrayList<String> factors = new ArrayList<>();
            if (args.length - i < 5) throw new IllegalArgumentException("NOT ENOUGH ARGUMENTS");
            method = args[i++];
            if (!METHODS.contains(method)) throw new IllegalArgumentException("WRONG METHOD ARGUMENT AT " + i);
            uri = args[i++];
            amount = Integer.parseInt(args[i++]);
            verifyAmount(amount, method);
            file = args[i++];
            while(i < args.length){
                if (!isFactor(args[i])) break;
                factors.add(args[i++]);
            }
            return new ReqConfig(method, uri, amount, file, factors.toArray(new String[factors.size()]), i);
        }

        private static void verifyAmount(int amount, String method) {
            if (method.equals(AmmoFactory.GET) && amount < 3) throw new IllegalArgumentException("MIN: 3 FOR GET");
            if (method.equals(AmmoFactory.PUT) && amount < 2) throw new IllegalArgumentException("MIN: 2 FOR PUT");
            if (method.equals(AmmoFactory.MIX) && amount < 10) throw new IllegalArgumentException("MIN: 10 FOR MIX");
        }

        private static boolean isFactor(String arg) {
            try {
                String[] parts = arg.split("=");
                if (parts.length != 2) return false;
                if (!parts[0].equals(REPLICAS)) return false;
                String[] factor = parts[1].split("/");
                if (factor.length != 2) return false;
                int ack = Integer.parseInt(factor[0]);
                int from = Integer.parseInt(factor[1]);
                return true;
            } catch (NumberFormatException nFE) {
                nFE.printStackTrace();
                throw new IllegalArgumentException("CORRUPTED FACTOR ARGUMENT");
            }
        }

        public void build(File dir, String mode) throws IOException{
            switch (mode) {
                case SPLITTED_MODE: {
                    for (String factor : this.factors) {
                        File dist = createFile(dir, this.file + PREFIX_SPLITTER + factor);
                        if (method.equals(AmmoFactory.GET)) AmmoFactory.generateGetAmmo(dist, uri, amount, factor);
                        if (method.equals(AmmoFactory.PUT)) AmmoFactory.generatePutAmmo(dist, uri, amount, factor);
                        if (method.equals(AmmoFactory.MIX)) AmmoFactory.generateMixedAmmo(dist, uri, amount, factor);
                    }
                    break;
                }
                case COMBINED_MODE: {
                    File dist = createFile(dir, this.file);
                    if (method.equals(AmmoFactory.GET)) AmmoFactory.generateGetAmmo(dist, uri, amount, factors);
                    if (method.equals(AmmoFactory.PUT)) AmmoFactory.generatePutAmmo(dist, uri, amount, factors);
                    if (method.equals(AmmoFactory.MIX)) AmmoFactory.generateMixedAmmo(dist, uri, amount, factors);
                    break;
                }
                default: throw new IllegalArgumentException("WRONG MODE: " + mode);
            }
        }

        private static File createFile(File dir, String filePath) throws IOException{
            File file = new File(dir.getAbsolutePath() + SLASH + filePath.replaceAll("[\0/]", "-"));
            if (file.exists()) {
                if (file.canWrite()) {
                    file.delete();
                } else throw new IOException("CANNOT REMOVE EXISTING FILE: " + file.toString());
            }
            if (!file.createNewFile()) throw new IOException("CANNOT CREATE FILE " + filePath);
            return file;
        }

        public int getOffset() {
            return offset;
        }
    }

    public static void main(String[] args) throws IOException{
        //mode dir method uri amount file factors
        if (args.length < MIN_ARGS_LENGTH) throw new IllegalArgumentException("NOT ENOUGH ARGUMENTS");
        int position = 0;
        String mode = args[position++];
        File dir = new File(args[position++]);
        if (!dir.isDirectory()) throw new IllegalArgumentException(dir.toString() + " IS NOT DIRECTORY");
        if (!dir.exists()) throw new IllegalArgumentException(dir.toString() + " IS NOT FOUND");
        System.out.println("Mode: " + mode);
        System.out.println("Folder: " + dir.getAbsolutePath());
        while(position < args.length) {
            ReqConfig reqConfig = ReqConfig.generate(args, position);
            position = reqConfig.getOffset();
            reqConfig.build(dir, mode);
        }
        System.out.println("Total: " + AmmoFactory.getRequestsAmount());
    }
}
