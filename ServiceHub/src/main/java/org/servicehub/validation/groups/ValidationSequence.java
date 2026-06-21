package org.servicehub.validation.groups;

import jakarta.validation.GroupSequence;

@GroupSequence({
        FirstGroup.class,
        SecondGroup.class
})
public interface ValidationSequence {
}
