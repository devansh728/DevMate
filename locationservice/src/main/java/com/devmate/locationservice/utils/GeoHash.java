package com.devmate.locationservice.utils;

import java.util.BitSet;
import java.util.Arrays;

/**
 * A utility class for Geohash encoding, decoding, and neighbor finding.
 *
 * This class provides a complete, self-contained implementation for working with Geohashes.
 * For production applications, it is generally recommended to use a proven library
 * that handles all edge cases and optimizations.
 */
public class GeoHash {

    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";
    private static final int[] BITS = {16, 8, 4, 2, 1};

    /**
     * Encodes a latitude and longitude into a Geohash string of a given precision.
     * @param latitude The latitude to encode.
     * @param longitude The longitude to encode.
     * @param precision The desired length of the Geohash string.
     * @return The encoded Geohash string.
     */
    public static String encode(double latitude, double longitude, int precision) {
        double[] latRange = {-90.0, 90.0};
        double[] lonRange = {-180.0, 180.0};
        boolean isEven = true;
        BitSet buffer = new BitSet(precision * 5);
        int bit = 0;

        while (bit < precision * 5) {
            if (isEven) {
                double mid = (lonRange[0] + lonRange[1]) / 2;
                if (longitude > mid) {
                    buffer.set(bit);
                    lonRange[0] = mid;
                } else {
                    lonRange[1] = mid;
                }
            } else {
                double mid = (latRange[0] + latRange[1]) / 2;
                if (latitude > mid) {
                    buffer.set(bit);
                    latRange[0] = mid;
                } else {
                    latRange[1] = mid;
                }
            }
            isEven = !isEven;
            bit++;
        }

        StringBuilder hash = new StringBuilder();
        for (int i = 0; i < precision; i++) {
            int idx = 0;
            for (int j = 0; j < 5; j++) {
                if (buffer.get(i * 5 + j)) {
                    idx += BITS[j];
                }
            }
            hash.append(BASE32.charAt(idx));
        }
        return hash.toString();
    }

    /**
     * Decodes a Geohash string to its approximate latitude and longitude coordinates.
     * @param geohash The Geohash string.
     * @return An array containing the decoded latitude and longitude.
     */
    public static double[] decode(String geohash) {
        double[] latRange = {-90.0, 90.0};
        double[] lonRange = {-180.0, 180.0};
        boolean isEven = true;

        for (char ch : geohash.toCharArray()) {
            int idx = BASE32.indexOf(ch);
            for (int i = 0; i < 5; i++) {
                int bit = (idx >> (4 - i)) & 1;
                if (isEven) {
                    double mid = (lonRange[0] + lonRange[1]) / 2;
                    if (bit == 1) {
                        lonRange[0] = mid;
                    } else {
                        lonRange[1] = mid;
                    }
                } else {
                    double mid = (latRange[0] + latRange[1]) / 2;
                    if (bit == 1) {
                        latRange[0] = mid;
                    } else {
                        latRange[1] = mid;
                    }
                }
                isEven = !isEven;
            }
        }
        return new double[]{(latRange[0] + latRange[1]) / 2, (lonRange[0] + lonRange[1]) / 2};
    }

    /**
     * Finds the 8 neighboring Geohash strings for a given Geohash.
     * This is crucial for proximity searches to handle grid boundary issues.
     * It uses a decode-offset-encode approach for reliability.
     * @param geohash The Geohash string.
     * @return An array of 8 neighbor Geohash strings.
     */
    public static String[] getAdjacentHashes(String geohash) {
        double[] coords = decode(geohash);
        double lat = coords[0];
        double lon = coords[1];
        int precision = geohash.length();

        // Approximate cell dimensions (degrees) for the given precision.
        // These are rough estimates; a more complex calculation would be more precise.
        double latDelta = 180.0 / Math.pow(2, precision * 2.5);
        double lonDelta = 360.0 / Math.pow(2, precision * 2.5);

        double[][] offsets = {
                {latDelta, 0},      // TOP
                {-latDelta, 0},     // BOTTOM
                {0, -lonDelta},     // LEFT
                {0, lonDelta},      // RIGHT
                {latDelta, -lonDelta},  // TOP-LEFT
                {latDelta, lonDelta},   // TOP-RIGHT
                {-latDelta, -lonDelta}, // BOTTOM-LEFT
                {-latDelta, lonDelta}   // BOTTOM-RIGHT
        };

        String[] adjacentHashes = new String[8];
        for (int i = 0; i < offsets.length; i++) {
            double newLat = lat + offsets[i][0];
            double newLon = lon + offsets[i][1];

            // Handle longitude wrap-around at 180/-180 degrees
            if (newLon > 180.0) {
                newLon -= 360.0;
            } else if (newLon < -180.0) {
                newLon += 360.0;
            }

            adjacentHashes[i] = encode(newLat, newLon, precision);
        }

        return adjacentHashes;
    }
}
