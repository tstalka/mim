package org.motechproject.nms.region.service;

import org.motechproject.nms.region.domain.Circle;

import java.util.Set;

public interface CircleService {
    Circle getByName(String name);
    Set<Circle> getAll();
}
