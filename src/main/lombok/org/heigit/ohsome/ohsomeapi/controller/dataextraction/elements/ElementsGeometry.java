package org.heigit.ohsome.ohsomeapi.controller.dataextraction.elements;

import org.springframework.stereotype.Component;

/** Enumeration defining the geometry of the OSM elements(RAW, BBOX, CENTROID). */
@Component
public enum ElementsGeometry {

  RAW, BBOX, CENTROID
}
