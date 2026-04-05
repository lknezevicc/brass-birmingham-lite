package hr.lknezevic.brassbirmingham.ui;

import hr.lknezevic.brassbirmingham.model.game.BoardEdge;
import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;

public record BoardPick(Kind kind, CityId city, int slotIndex, IndustryType slotType, BoardEdge edge) {

    public enum Kind {
        NONE,
        CITY,
        SLOT,
        EDGE
    }
}
