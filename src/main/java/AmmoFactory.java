import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

import static java.lang.Math.abs;

public class AmmoFactory extends FactoryBase{

    // Header
    public static final String GET = "GET";
    public static final String PUT = "PUT";
    public static final String MIX = "MIX";

    private static final String HTTP_VERSION = "HTTP/1.0";
    private static final String CONTENT_LENGTH = "Content-Length: ";

    private static final String CONNECTION_CLOSE = "Connection: Close";

    // Templates
    private static final String URI_SPLITTER = "?";
    private static final String QUERY_SPLITTER = "&";

    private static final String ID = "id=";

    private static final String WHITE_SPACE = " ";
    private static final String RN = "\r\n";
    private static final String NEW_LINE = "\n";

    private static int requestsAmount = 0;

    public static int getRequestsAmount() {
        return requestsAmount;
    }

    public static void generatePutAmmo(final File dist, final String uri, final int amount, String... replicas) {
        BigInteger value = initial();
        try (FileOutputStream fOS = new FileOutputStream(dist)) {
            for (String factor : replicas) {
                putRequests(uri, amount / replicas.length, factor, value, fOS);
            }

            fOS.close();

            System.out.println("Created PUT ammo at " + dist.getAbsolutePath());
        } catch (IOException iOE) {
            iOE.printStackTrace();
        }
    }

    public static void generatePutAmmo(final File dist, final String uri, final int amount, String factor) {
        BigInteger value = initial();
        try (FileOutputStream fOS = new FileOutputStream(dist)) {
            putRequests(uri, amount, factor, value, fOS);
            fOS.close();
            System.out.println("Created PUT ammo at " + dist.getAbsolutePath());
        } catch (IOException iOE) {
            iOE.printStackTrace();
        }
    }

    public static void generateStatusAmmo(final String uri, final File dist, int amount){
        try (FileOutputStream fOS = new FileOutputStream(dist)) {
            byte[] bytes = new StringBuilder()
                    .append(GET).append(WHITE_SPACE)
                    .append(uri).append(WHITE_SPACE)
                    .append(HTTP_VERSION).append(RN)
                    .append(RN)
                    .toString().getBytes();

            fOS.write(Integer.toString(bytes.length).getBytes());
            fOS.write(NEW_LINE.getBytes());
            fOS.write(bytes);

            fOS.close();
            System.out.println("Created STATUS ammo at " + dist.getAbsolutePath());
        } catch (IOException iOE) {
            iOE.printStackTrace();
        }
    }

    public static void generateGetAmmo(final File dist, final String uri, final int amount, String... replicas) {
        try (FileOutputStream fOS = new FileOutputStream(dist)) {
            BigInteger value = initial();
            Random random = new Random();

            for (String factor : replicas) {
                getRequests(uri, amount / replicas.length, factor, fOS, value, random);
            }

            fOS.close();
            System.out.println("Created GET ammo at " + dist.getAbsolutePath());
        } catch (IOException iOE) {
            iOE.printStackTrace();
        }
    }

    public static void generateMixedAmmo(final File dist, final String uri, final int amount, String... replicas) {
        try (FileOutputStream fOS = new FileOutputStream(dist)) {
            BigInteger value = initial();
            Random random = new Random();
            for (String factor : replicas) {
                putRequests(uri, (amount / 4) / replicas.length, factor, value, fOS);
                getRequests(uri, (amount / 4) / replicas.length, factor, fOS, value, random);
                putRequests(uri, (amount / 8) / replicas.length, factor, value, fOS);
                getRequests(uri, (amount / 4) / replicas.length, factor, fOS, value, random);
            }

            fOS.close();
            System.out.println("Created GET ammo at " + dist.getAbsolutePath());
        } catch (IOException iOE) {
            iOE.printStackTrace();
        }
    }

    public static void generateGetAmmo(final File dist, final String uri, final int amount, String factor) {
        try (FileOutputStream fOS = new FileOutputStream(dist)) {
            BigInteger value = initial();
            Random random = new Random();
            getRequests(uri, amount, factor, fOS, value, random);
            fOS.close();
            System.out.println("Created GET ammo at " + dist.getAbsolutePath());
        } catch (IOException iOE) {
            iOE.printStackTrace();
        }
    }

    public static void generateMixedAmmo(final File dist, final String uri, final int amount, String factor) {
        try (FileOutputStream fOS = new FileOutputStream(dist)) {
            BigInteger value = initial();
            Random random = new Random();

            putRequests(uri, amount / 4, factor, value, fOS);
            getRequests(uri, amount / 4, factor, fOS, value, random);
            putRequests(uri, amount / 8, factor, value, fOS);
            getRequests(uri, amount / 4, factor, fOS, value, random);

            fOS.close();
            System.out.println("Created GET ammo at " + dist.getAbsolutePath());
        } catch (IOException iOE) {
            iOE.printStackTrace();
        }
    }

    private static void putRequests(String uri, int amount, String factor, BigInteger value, FileOutputStream fOS) throws IOException {
        int innerAmount = amount / 2;
        if (innerAmount == 0) innerAmount = 1;
        for (int i = 0; i < innerAmount; i++) {
            byte[] body = new StringBuilder()
                    .append(value)
                    .append(RN)
                    .toString().getBytes();
            byte[] header = new StringBuilder()
                    .append(PUT).append(WHITE_SPACE)
                    .append(uri).append(URI_SPLITTER).append(ID).append(i).append(QUERY_SPLITTER).append(factor).append(WHITE_SPACE)
                    .append(HTTP_VERSION).append(RN)
                    .append(CONTENT_LENGTH).append(body.length).append(RN)
                    .append(RN)
                    .toString().getBytes();

            fOS.write(Integer.toString(header.length + body.length).getBytes());
            fOS.write(NEW_LINE.getBytes());
            fOS.write(header);
            fOS.write(body);
            value = next(value);
            requestsAmount++;
        }

        Random random = new Random();
        for (int i = 0; i < innerAmount; i++) {
            int key = 0 + abs(random.nextInt()) % (innerAmount);
            byte[] body = new StringBuilder()
                    .append(value)
                    .append(RN)
                    .toString().getBytes();
            byte[] header = new StringBuilder()
                    .append(PUT).append(WHITE_SPACE)
                    .append(uri).append(URI_SPLITTER).append(ID).append(key).append(QUERY_SPLITTER).append(factor).append(WHITE_SPACE)
                    .append(HTTP_VERSION).append(RN)
                    .append(CONTENT_LENGTH).append(body.length).append(RN)
                    .append(RN)
                    .toString().getBytes();

            fOS.write(Integer.toString(header.length + body.length).getBytes());
            fOS.write(NEW_LINE.getBytes());
            fOS.write(header);
            fOS.write(body);
            value = next(value);
            requestsAmount++;
        }
    }

    private static void getRequests(String uri, int amount, String factor, FileOutputStream fOS, BigInteger value, Random random) throws IOException {
        int innerAmount = amount / 3;
        if (innerAmount == 0) innerAmount = 1;
        for (int i = 0; i < innerAmount; i++) {
            byte[] header = new StringBuilder()
                    .append(GET).append(WHITE_SPACE)
                    .append(uri).append(URI_SPLITTER).append(ID).append(i).append(QUERY_SPLITTER).append(factor).append(WHITE_SPACE)
                    .append(HTTP_VERSION).append(RN)
                    .append(RN)
                    .toString().getBytes();

            fOS.write(Integer.toString(header.length).getBytes());
            fOS.write(NEW_LINE.getBytes());
            fOS.write(header);
            value = next(value);
            requestsAmount++;
        }
        for (int i = innerAmount - 1; i >= 0; i--) {
            byte[] header = new StringBuilder()
                    .append(GET).append(WHITE_SPACE)
                    .append(uri).append(URI_SPLITTER).append(ID).append(i).append(QUERY_SPLITTER).append(factor).append(WHITE_SPACE)
                    .append(HTTP_VERSION).append(RN)
                    .append(RN)
                    .toString().getBytes();

            fOS.write(Integer.toString(header.length).getBytes());
            fOS.write(NEW_LINE.getBytes());
            fOS.write(header);
            value = next(value);
            requestsAmount++;
        }
        for (int i = 0; i < innerAmount; i++) {
            int key = 0 + abs(random.nextInt()) % (innerAmount);
            byte[] header = new StringBuilder()
                    .append(GET).append(WHITE_SPACE)
                    .append(uri).append(URI_SPLITTER).append(ID).append(key).append(QUERY_SPLITTER).append(factor).append(WHITE_SPACE)
                    .append(HTTP_VERSION).append(RN)
                    .append(RN)
                    .toString().getBytes();

            fOS.write(Integer.toString(header.length).getBytes());
            fOS.write(NEW_LINE.getBytes());
            fOS.write(header);
            value = next(value);
            requestsAmount++;
        }
    }
}
