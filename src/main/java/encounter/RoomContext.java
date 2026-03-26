package encounter;

/** Context passed to each encounter so it knows which room it is. */
public record RoomContext(int roomIndex) {}
