package hr.lknezevic.brassbirmingham.model.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Getter
@RequiredArgsConstructor
public final class PlacedLink implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int ownerId;
    private final BoardEdge edge;
    private final LinkType linkType;
}
