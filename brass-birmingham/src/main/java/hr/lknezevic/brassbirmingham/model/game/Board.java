package hr.lknezevic.brassbirmingham.model.game;

import hr.lknezevic.brassbirmingham.model.industry.IndustryLevel;
import hr.lknezevic.brassbirmingham.model.industry.IndustryType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public final class Board implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<PlacedIndustry> placedIndustries = new ArrayList<>();
    private final List<PlacedLink> placedLinks = new ArrayList<>();
    private final Market market;
    private int merchantBeer;

    public Board() {
        this.market = new Market();
        this.merchantBeer = 1;
    }

    public List<PlacedIndustry> getPlacedIndustries() { return placedIndustries; }
    public List<PlacedLink> getPlacedLinks() { return placedLinks; }
    public Market getMarket() { return market; }
    public int getMerchantBeer() { return merchantBeer; }

    public void addIndustry(PlacedIndustry industry) {
        placedIndustries.add(industry);
    }

    public void addLink(PlacedLink link) {
        placedLinks.add(link);
    }

    public void consumeMerchantBeer() {
        if (merchantBeer > 0) merchantBeer--;
    }

    public void resetMerchantBeer() {
        merchantBeer = 1;
    }

    public boolean isEdgeOccupied(BoardEdge edge) {
        return placedLinks.stream().anyMatch(l -> l.getEdge().equals(edge));
    }

    public List<PlacedIndustry> industriesAt(CityId city) {
        return placedIndustries.stream()
                .filter(p -> p.getCity() == city)
                .toList();
    }

    public List<PlacedIndustry> industriesOwnedBy(int playerId) {
        return placedIndustries.stream()
                .filter(p -> p.getOwnerId() == playerId)
                .toList();
    }

    public long countPlayerIndustriesAt(int playerId, CityId city) {
        return placedIndustries.stream()
                .filter(p -> p.getOwnerId() == playerId && p.getCity() == city)
                .count();
    }

    public boolean hasSlotAvailable(CityId city, IndustryType type) {
        List<IndustryType> slots = BoardDefinition.CITY_SLOTS.get(city);
        if (slots == null || !slots.contains(type)) return false;
        long used = placedIndustries.stream()
                .filter(p -> p.getCity() == city && p.getType() == type)
                .count();
        long available = slots.stream().filter(s -> s == type).count();
        return used < available;
    }

    public void removeLevel1Industries() {
        placedIndustries.removeIf(p -> p.getLevel() == IndustryLevel.L1);
    }

    public void removeAllLinks() {
        placedLinks.clear();
    }
}
