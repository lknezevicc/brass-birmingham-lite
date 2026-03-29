package hr.lknezevic.brassbirmingham.ui;

import hr.lknezevic.brassbirmingham.model.game.BoardDefinition;
import hr.lknezevic.brassbirmingham.model.game.BoardEdge;
import hr.lknezevic.brassbirmingham.model.game.CityId;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BoardLayoutTest {

    @Test
    void allCitiesHavePositions() {
        Map<CityId, Point2D> positions = BoardLayout.getAllCityPositions();
        assertThat(positions).hasSize(5);
        for (CityId city : CityId.values()) {
            assertThat(positions).containsKey(city);
            Point2D pos = positions.get(city);
            assertThat(pos.getX()).isBetween(0.0, BoardLayout.CANVAS_WIDTH);
            assertThat(pos.getY()).isBetween(0.0, BoardLayout.CANVAS_HEIGHT);
        }
    }

    @Test
    void allCitiesHaveTwoSlotBounds() {
        Map<CityId, List<Rectangle2D>> allSlots = BoardLayout.getAllSlotBounds();
        assertThat(allSlots).hasSize(5);
        for (CityId city : CityId.values()) {
            List<Rectangle2D> slots = allSlots.get(city);
            assertThat(slots).hasSize(2);
            for (Rectangle2D rect : slots) {
                assertThat(rect.getWidth()).isEqualTo(BoardLayout.SLOT_WIDTH);
                assertThat(rect.getHeight()).isEqualTo(BoardLayout.SLOT_HEIGHT);
            }
        }
    }

    @Test
    void slotTypesMatchBoardDefinition() {
        for (CityId city : CityId.values()) {
            List<IndustryType> expected = BoardDefinition.CITY_SLOTS.get(city);
            for (int i = 0; i < expected.size(); i++) {
                assertThat(BoardLayout.getSlotType(city, i)).isEqualTo(expected.get(i));
            }
        }
    }

    @Test
    void allEdgesHaveMidpoints() {
        for (BoardEdge edge : BoardDefinition.EDGES) {
            Point2D mid = BoardLayout.getEdgeMidpoint(edge);
            assertThat(mid).isNotNull();
            Point2D a = BoardLayout.getCityCenter(edge.getCityA());
            Point2D b = BoardLayout.getCityCenter(edge.getCityB());
            assertThat(mid.getX()).isEqualTo((a.getX() + b.getX()) / 2, org.assertj.core.data.Offset.offset(0.01));
            assertThat(mid.getY()).isEqualTo((a.getY() + b.getY()) / 2, org.assertj.core.data.Offset.offset(0.01));
        }
    }
}
