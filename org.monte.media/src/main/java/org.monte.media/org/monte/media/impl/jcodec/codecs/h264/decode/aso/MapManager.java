package org.monte.media.impl.jcodec.codecs.h264.decode.aso;

import org.monte.media.impl.jcodec.codecs.h264.io.model.PictureParameterSet;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SeqParameterSet;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SliceHeader;

/**
 * References:
 * <p>
 * This code has been derived from JCodecProject.
 * <dl>
 *     <dt>JCodecProject. Copyright 2008-2019 JCodecProject.
 *     <br><a href="https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE">BSD 2-Clause License.</a></dt>
 *     <dd><a href="https://github.com/jcodec/jcodec">github.com</a></dd>
 * </dl>
 *
 * @author The JCodec project
 */
public class MapManager {
    private SeqParameterSet sps;
    private PictureParameterSet pps;
    private MBToSliceGroupMap mbToSliceGroupMap;
    private int prevSliceGroupChangeCycle;

    public MapManager(SeqParameterSet sps, PictureParameterSet pps) {
        this.sps = sps;
        this.pps = pps;
        this.mbToSliceGroupMap = buildMap(sps, pps);
    }

    private MBToSliceGroupMap buildMap(SeqParameterSet sps, PictureParameterSet pps) {
        int numGroups = pps.numSliceGroupsMinus1 + 1;

        if (numGroups > 1) {
            int[] map;
            int picWidthInMbs = sps.picWidthInMbsMinus1 + 1;
            int picHeightInMbs = SeqParameterSet.getPicHeightInMbs(sps);

            if (pps.sliceGroupMapType == 0) {
                int[] runLength = new int[numGroups];
                for (int i = 0; i < numGroups; i++) {
                    runLength[i] = pps.runLengthMinus1[i] + 1;
                }
                map = SliceGroupMapBuilder.buildInterleavedMap(picWidthInMbs, picHeightInMbs, runLength);
            } else if (pps.sliceGroupMapType == 1) {
                map = SliceGroupMapBuilder.buildDispersedMap(picWidthInMbs, picHeightInMbs, numGroups);
            } else if (pps.sliceGroupMapType == 2) {
                map = SliceGroupMapBuilder.buildForegroundMap(picWidthInMbs, picHeightInMbs, numGroups, pps.topLeft,
                        pps.bottomRight);
            } else if (pps.sliceGroupMapType >= 3 && pps.sliceGroupMapType <= 5) {
                return null;
            } else if (pps.sliceGroupMapType == 6) {
                map = pps.sliceGroupId;
            } else {
                throw new RuntimeException("Unsupported slice group map type");
            }

            return buildMapIndices(map, numGroups);
        }

        return null;
    }

    private MBToSliceGroupMap buildMapIndices(int[] map, int numGroups) {
        int[] ind = new int[numGroups];
        int[] indices = new int[map.length];

        for (int i = 0; i < map.length; i++) {
            indices[i] = ind[map[i]]++;
        }

        int[][] inverse = new int[numGroups][];
        for (int i = 0; i < numGroups; i++) {
            inverse[i] = new int[ind[i]];
        }
        ind = new int[numGroups];
        for (int i = 0; i < map.length; i++) {
            int sliceGroup = map[i];
            inverse[sliceGroup][ind[sliceGroup]++] = i;
        }

        return new MBToSliceGroupMap(map, indices, inverse);
    }

    private void updateMap(SliceHeader sh) {
        int mapType = pps.sliceGroupMapType;
        int numGroups = pps.numSliceGroupsMinus1 + 1;

        if (numGroups > 1 && mapType >= 3 && mapType <= 5
                && (sh.sliceGroupChangeCycle != prevSliceGroupChangeCycle || mbToSliceGroupMap == null)) {

            prevSliceGroupChangeCycle = sh.sliceGroupChangeCycle;

            int picWidthInMbs = sps.picWidthInMbsMinus1 + 1;
            int picHeightInMbs = SeqParameterSet.getPicHeightInMbs(sps);
            int picSizeInMapUnits = picWidthInMbs * picHeightInMbs;
            int mapUnitsInSliceGroup0 = sh.sliceGroupChangeCycle * (pps.sliceGroupChangeRateMinus1 + 1);
            mapUnitsInSliceGroup0 = mapUnitsInSliceGroup0 > picSizeInMapUnits ? picSizeInMapUnits
                    : mapUnitsInSliceGroup0;

            int sizeOfUpperLeftGroup = (pps.sliceGroupChangeDirectionFlag ? (picSizeInMapUnits - mapUnitsInSliceGroup0)
                    : mapUnitsInSliceGroup0);

            int[] map;
            if (mapType == 3) {
                map = SliceGroupMapBuilder.buildBoxOutMap(picWidthInMbs, picHeightInMbs,
                        pps.sliceGroupChangeDirectionFlag, mapUnitsInSliceGroup0);
            } else if (mapType == 4) {
                map = SliceGroupMapBuilder.buildRasterScanMap(picWidthInMbs, picHeightInMbs, sizeOfUpperLeftGroup,
                        pps.sliceGroupChangeDirectionFlag);
            } else {
                map = SliceGroupMapBuilder.buildWipeMap(picWidthInMbs, picHeightInMbs, sizeOfUpperLeftGroup,
                        pps.sliceGroupChangeDirectionFlag);
            }

            this.mbToSliceGroupMap = buildMapIndices(map, numGroups);
        }
    }

    public Mapper getMapper(SliceHeader sh) {
        updateMap(sh);
        int firstMBInSlice = sh.firstMbInSlice;
        if (pps.numSliceGroupsMinus1 > 0) {

            return new PrebuiltMBlockMapper(mbToSliceGroupMap, firstMBInSlice, sps.picWidthInMbsMinus1 + 1);
        } else {
            return new FlatMBlockMapper(sps.picWidthInMbsMinus1 + 1, firstMBInSlice);
        }
    }
}
