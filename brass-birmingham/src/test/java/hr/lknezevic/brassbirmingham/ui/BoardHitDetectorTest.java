package hr.lknezevic.brassbirmingham.ui;

import hr.lknezevic.brassbirmingham.model.game.BoardDefinition;
import hr.lknezevic.brassbirmingham.model.game.BoardEdge;
import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BoardHitDetectorTest {

    @Test
    void clickInsideSlotReturnsSLOT() {
        CityId city = CityId.BIRMINGHAM;
        List<Rectangle2D> slots = BoardLayout.getSlotBounds(city);
        Rectangle2D slot0 = slots.get(0);
        double cx = slot0.getMinX() + slot0.getWidth() / 2;
        double cy = slot0.getMinY() + slot0.getHeight() / 2;

        BoardPick pick = BoardHitDetector.pick(cx, cy);
        assertThat(pick.kind()).isEqualTo(BoardPick.Kind.SLOT);
        assertThat(pick.city()).isEqualTo(CityId.BIRMINGHAM);
        assertThat(pick.slotIndex()).isEqualTo(0);

        List<IndustryType> citySlots = BoardDefinition.CITY_SLOTS.get(CityId.BIRMINGHAM);
        assertThat(pick.slotType()).isEqualTo(citySlots.get(0));
    }

    @Test
    void clickOnCityCenterReturnsCITY() {
        Point2D center = BoardLayout.getCityCenter(CityId.COVENTRY);
        BoardPick pick = BoardHitDetector.pick(center.getX(), center.getY());
        assertThat(pick.kind()).isEqualTo(BoardPick.Kind.CITY);
        assertThat(pick.city()).isEqualTo(CityId.COVENTRY);
    }

    @Test
    void clickNearEdgeMidpointReturnsEDGE() {
        BoardEdge edge = BoardDefinition.EDGES.get(0);
        Point2D mid = BoardLayout.getEdgeMidpoint(edge);
        BoardPick pick = BoardHitDetector.pick(mid.getX(), mid.getY());
        assertThat(pick.kind()).isEqualTo(BoardPick.Kind.EDGE);
        assertThat(pick.edge()).isEqualTo(edge);
    }

    @Test
    void clickInEmptyAreaReturnsNONE() {
        BoardPick pick = BoardHitDetector.pick(0, 0);
        assertThat(pick.kind()).isEqualTo(BoardPick.Kind.NONE);
    }
}
