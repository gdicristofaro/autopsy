/*
 * Autopsy Forensic Browser
 *
 * Copyright 2019 Basis Technology Corp.
 * contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.geolocation.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.BlackboardArtifact;
import org.sleuthkit.datamodel.BlackboardAttribute;
import org.sleuthkit.datamodel.SleuthkitCase;
import org.sleuthkit.datamodel.TskCoreException;

/**
 * The basic details of a waypoint.
 *
 */
public interface Waypoint {
    static final Logger logger = Logger.getLogger(Waypoint.class.getName());
    
    /**
     * This is a list of attributes that are already being handled by the
     * waypoint classes and will have get functions.
     */
    BlackboardAttribute.ATTRIBUTE_TYPE[] ALREADY_HANDLED_ATTRIBUTES = {
        BlackboardAttribute.ATTRIBUTE_TYPE.TSK_NAME,
        BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_LONGITUDE,
        BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_LATITUDE,
        BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_ALTITUDE,
        BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME,
        BlackboardAttribute.ATTRIBUTE_TYPE.TSK_DATETIME_CREATED,
        BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_LATITUDE_START,
        BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_LONGITUDE_START,
        BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_LATITUDE_END,
        BlackboardAttribute.ATTRIBUTE_TYPE.TSK_GEO_LONGITUDE_END,};

    /**
     * Interface to describe a waypoint.  A waypoint is made up of 
     * a longitude, latitude, label, timestamp, type, image and altitude.
     * 
     * A good way point should have at minimum a longitude and latutude.
     *
     * @return Timestamp in java/unix epoch seconds or null if none was set.
     */
    Long getTimestamp();

    /**
     * Get the label for this point object.
     *
     * @return String label for the point or null if none was set
     */
    String getLabel();

    /**
     * Get the latitude for this point.
     *
     * @return Returns the latitude for the point or null if none was set
     */
    Double getLatitude();

    /**
     * Get the longitude for this point.
     *
     * @return Returns the longitude for the point or null if none was set
     */
    Double getLongitude();

    /**
     * Get the altitude for this point.
     *
     * @return Returns the altitude for the point or null if none was set
     */
    Double getAltitude();

    /**
     * Gets an unmodifiable List of other properties that may be interesting to this way point.
     * The List will not include properties for which getter functions
     * exist.
     *
     * @return A List of waypoint properties
     */
    List<Property> getOtherProperties();

    /**
     * Get the image for this waypoint.
     *
     * @return AbstractFile image or null if one was not set
     */
    AbstractFile getImage();

    /**
     * Returns a list of Waypoints for the artifacts with geolocation
     * information.
     *
     * List will include artifacts of type: TSK_GPS_TRACKPOINT TSK_GPS_SEARCH
     * TSK_GPS_LAST_KNOWN_LOCATION TSK_GPS_BOOKMARK TSK_METADATA_EXIF
     *
     * @param skCase Currently open SleuthkitCase
     *
     * @return List of Waypoint
     *
     * @throws GeoLocationDataException
     */
    static List<Waypoint> getAllWaypoints(SleuthkitCase skCase) throws GeoLocationDataException {
        List<Waypoint> points = new ArrayList<>();

        points.addAll(getTrackpointWaypoints(skCase));
        points.addAll(getEXIFWaypoints(skCase));
        points.addAll(getSearchWaypoints(skCase));
        points.addAll(getLastKnownWaypoints(skCase));
        points.addAll(getBookmarkWaypoints(skCase));

        return points;
    }

    /**
     * Gets a list of Waypoints for TSK_GPS_TRACKPOINT artifacts.
     *
     * @param skCase Currently open SleuthkitCase
     *
     * @return List of Waypoint
     *
     * @throws GeoLocationDataException
     */
    static List<Waypoint> getTrackpointWaypoints(SleuthkitCase skCase) throws GeoLocationDataException {
        List<BlackboardArtifact> artifacts = null;
        try{
            artifacts = skCase.getBlackboardArtifacts(BlackboardArtifact.ARTIFACT_TYPE.TSK_GPS_TRACKPOINT);
        } catch(TskCoreException ex) {
            throw new GeoLocationDataException("Unable to get artifacts for type: TSK_GPS_TRACKPOINT", ex);
        }
        
        List<Waypoint> points = new ArrayList<>();
        for (BlackboardArtifact artifact : artifacts) {
            try{
                ArtifactWaypoint point = new TrackpointWaypoint(artifact);
                points.add(point);
            } catch(GeoLocationDataException ex) {
                logger.log(Level.WARNING, String.format("No longitude or latitude available for TSK_GPS_TRACKPOINT artifactID: %d", artifact.getArtifactID()));
            }       
        }
        return points;
    }

    /**
     * Gets a list of Waypoints for TSK_METADATA_EXIF artifacts.
     *
     * @param skCase Currently open SleuthkitCase
     *
     * @return List of Waypoint
     *
     * @throws GeoLocationDataException
     */
    static List<Waypoint> getEXIFWaypoints(SleuthkitCase skCase) throws GeoLocationDataException {
        List<BlackboardArtifact> artifacts = null;
        try{
            artifacts = skCase.getBlackboardArtifacts(BlackboardArtifact.ARTIFACT_TYPE.TSK_METADATA_EXIF);
        } catch(TskCoreException ex) {
            throw new GeoLocationDataException("Unable to get artifacts for type: TSK_GPS_LAST_KNOWN_LOCATION", ex);
        }
        
        List<Waypoint> points = new ArrayList<>();
        if (artifacts != null) {
            for (BlackboardArtifact artifact : artifacts) {
                try{
                    ArtifactWaypoint point = new EXIFWaypoint(artifact);
                    points.add(point);
                } catch(GeoLocationDataException ex) {
                    // I am a little relucant to log this error because I suspect
                    // this will happen more often than not. It is valid for
                    // METADAT_EXIF to not have longitude and latitude
                }  
            }
        }
        return points;
    }

    /**
     * Gets a list of Waypoints for TSK_GPS_SEARCH artifacts.
     *
     * @param skCase Currently open SleuthkitCase
     *
     * @return List of Waypoint
     *
     * @throws GeoLocationDataException
     */
    static List<Waypoint> getSearchWaypoints(SleuthkitCase skCase) throws GeoLocationDataException {
        List<BlackboardArtifact> artifacts = null;
        try{
            artifacts = skCase.getBlackboardArtifacts(BlackboardArtifact.ARTIFACT_TYPE.TSK_GPS_SEARCH);
        } catch(TskCoreException ex) {
            throw new GeoLocationDataException("Unable to get artifacts for type: TSK_GPS_SEARCH", ex);
        }
        
        List<Waypoint> points = new ArrayList<>();
        if (artifacts != null) {
            for (BlackboardArtifact artifact : artifacts) {
                 try{
                    ArtifactWaypoint point = new SearchWaypoint(artifact);
                    points.add(point);
                } catch(GeoLocationDataException ex) {
                    logger.log(Level.WARNING, String.format("No longitude or latitude available for TSK_GPS_SEARCH artifactID: %d", artifact.getArtifactID()));
                }       
            }
        }
        return points;
    }

    /**
     * Gets a list of Waypoints for TSK_GPS_LAST_KNOWN_LOCATION artifacts.
     *
     * @param skCase Currently open SleuthkitCase
     *
     * @return List of Waypoint
     *
     * @throws GeoLocationDataException
     */
    static List<Waypoint> getLastKnownWaypoints(SleuthkitCase skCase) throws GeoLocationDataException {
        List<BlackboardArtifact> artifacts = null;
        try{
            artifacts = skCase.getBlackboardArtifacts(BlackboardArtifact.ARTIFACT_TYPE.TSK_GPS_LAST_KNOWN_LOCATION);
        } catch(TskCoreException ex) {
            throw new GeoLocationDataException("Unable to get artifacts for type: TSK_GPS_LAST_KNOWN_LOCATION", ex);
        }
        
        List<Waypoint> points = new ArrayList<>();
        if (artifacts != null) {
            for (BlackboardArtifact artifact : artifacts) {                
                try{
                    ArtifactWaypoint point = new LastKnownWaypoint(artifact);
                    points.add(point);
                } catch(GeoLocationDataException ex) {
                    logger.log(Level.WARNING, String.format("No longitude or latitude available for TSK_GPS_LAST_KNOWN_LOCATION artifactID: %d", artifact.getArtifactID()));
                }       
            }
        }
        return points;
    }

    /**
     * Gets a list of Waypoints for TSK_GPS_BOOKMARK artifacts.
     *
     * @param skCase Currently open SleuthkitCase
     *
     * @return List of Waypoint
     *
     * @throws GeoLocationDataException
     */
    static List<Waypoint> getBookmarkWaypoints(SleuthkitCase skCase) throws GeoLocationDataException {
        List<BlackboardArtifact> artifacts = null;
        try{
            artifacts = skCase.getBlackboardArtifacts(BlackboardArtifact.ARTIFACT_TYPE.TSK_GPS_BOOKMARK);
        } catch(TskCoreException ex) {
            throw new GeoLocationDataException("Unable to get artifacts for type: TSK_GPS_BOOKMARK", ex);
        }
        
        List<Waypoint> points = new ArrayList<>();
        if (artifacts != null) {
            for (BlackboardArtifact artifact : artifacts) {
                try{
                    ArtifactWaypoint point = new ArtifactWaypoint(artifact);
                    points.add(point);
                } catch(GeoLocationDataException ex) {
                    logger.log(Level.WARNING, String.format("No longitude or latitude available for TSK_GPS_BOOKMARK artifactID: %d", artifact.getArtifactID()));
                }               
            }
        }
        return points;
    }
    
        
    /**
     * Get a list of Waypoint.Property objects for the given artifact. This list
     * will not include attributes that the Waypoint interfact has get functions
     * for.
     *
     * @param artifact Blackboard artifact to get attributes\properties from
     *
     * @return A List of Waypoint.Property objects
     *
     * @throws GeoLocationDataException
     */
    static List<Waypoint.Property> createGeolocationProperties(Map<BlackboardAttribute.ATTRIBUTE_TYPE, BlackboardAttribute> attributeMap) throws GeoLocationDataException {
        List<Waypoint.Property> list = new ArrayList<>();

        Set<BlackboardAttribute.ATTRIBUTE_TYPE> keys = attributeMap.keySet();

        for (BlackboardAttribute.ATTRIBUTE_TYPE type : ALREADY_HANDLED_ATTRIBUTES) {
            keys.remove(type);
        }

        for (BlackboardAttribute.ATTRIBUTE_TYPE type : keys) {
            String key = type.getDisplayName();
            String value = attributeMap.get(type).getDisplayString();

            list.add(new Waypoint.Property(key, value));
        }
        return list;
    }

    /**
     * Simple property class for waypoint properties that a purely
     * informational.
     */
    class Property {

        private final String displayName;
        private final String value;

        /**
         * Construct a Property object.
         *
         * @param displayName String display name for property. Ideally not null
         *                    or empty string.
         * @param value       String value for property. Can be null.
         */
        Property(String displayName, String value) {
            this.displayName = displayName;
            this.value = value;
        }

        /**
         * Get the display name for this property.
         *
         * @return String display name.
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Get the property value.
         *
         * @return String value.
         */
        public String getValue() {
            return value;
        }
    }
}