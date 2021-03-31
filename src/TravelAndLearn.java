import lejos.robotics.navigation.MovePilot;
import lejos.robotics.subsumption.Behavior;

public class TravelAndLearn implements Behavior {

    public  boolean    suppressed;
    private PilotRobot me;
    private MovePilot  pilot;

    private static final int ROW_COUNT = 6, COL_COUNT = 7;
    private static final int CELL_SIZE        = 25;
    private static final int TURN_RIGHT_ANGLE = 90;

    private static Coordinate coord;
    private static Coordinate coordBeforeDetour;
    private static int        directionBeforeDetour;

    private static int pilotDirection;

    public TravelAndLearn(PilotRobot robot) {
        me = robot;
        pilot = me.getPilot();
        coord = new Coordinate(0, 0);
    }

    public void action() {

        while (true) {
            if (pilot.isMoving() || suppressed) {
                try {
                    Thread.sleep(500L);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Coordinate coord = getRobotCoordinate();
            // System.out.printf("current coord:%s, pilotDirection:%d.\n", coord.toString(),
            // pilotDirection);
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Coordinate leftCell = leftCellOf(coord);
            Coordinate rightCell = rightCellOf(coord);
            Coordinate frontCell = frontCellOf(coord);

            // if (frontCell != null) {
            // System.out.printf("front coord:%s, pilotDirection:%d.\n",
            // frontCell.toString(), pilotDirection);
            // }

            if (PilotMonitor.isAllDetected()) {// Detection finished
                // System.out.println("pilot stop:lrf all detected");
                pilot.stop();
                break;
            }

            // -------------------------------------- Detour detection
            // --------------------------------------
            if (isDetouring()) {
                if (isDetourFinished()) {
                    detourFinished();
                    // System.out.println("Detour finished, Turn right");
                    rotate(TURN_RIGHT_ANGLE);// Turn back
                    saveDetectedResult(coord, 0);
                    continue;
                }
                if (!hasObstacleFront()) {// No obstacle in front
                    // System.out.println("No obstacle in front, Step in");
                    forwardOneCell();
                    rotate(-TURN_RIGHT_ANGLE);// Turn left
                    continue;
                } else {
                    // System.out.println("Obstacle in front, Turn right");
                    rotate(TURN_RIGHT_ANGLE);// Turn right
                    continue;
                }
            }

            // -------------------------------------- Edge detection
            // --------------------------------------
            if (frontCell == null || hasDetected(frontCell)) {
                if (frontCell == null) {
                    // System.out.printf("Edge, Turn right. frontCell:null\n");
                } else {
                    // System.out.printf("FrontCellDetected, Turn right. frontCell:%s, value:%d\n",
                    // frontCell.toString(),
                    PilotMonitor.getArrayValue(frontCell.x, frontCell.y);
                }
                rotate(TURN_RIGHT_ANGLE);// Turn right
                continue;
            }

            // -------------------------------------- Frontcell detection
            // --------------------------------------
            if (!hasObstacleFront()) {
                // System.out.println("No obstacle in front, step in");
                forwardOneCell();// No obstacle in front, step in
                PilotMonitor.setArrayValue(frontCell.x, frontCell.y, 0);
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                // System.out.println("Obstacle in front, Turn right");
                // Detour begin
                coordBeforeDetour = coord;
                directionBeforeDetour = pilotDirection;
                rotate(TURN_RIGHT_ANGLE);// Obstacle in front,Turn right
                saveDetectedResult(frontCell, 1);
            }
        }
    }

    //update robot's coordinate after moving forward
    public void forwardOneCell() {
        pilot.travel(CELL_SIZE);
        switch (pilotDirection) {
            case 0:
                coord = new Coordinate(coord.x, coord.y + 1);
                break;
            case TURN_RIGHT_ANGLE:
                coord = new Coordinate(coord.x + 1, coord.y);
                break;
            case 2 * TURN_RIGHT_ANGLE:
                coord = new Coordinate(coord.x, coord.y - 1);
                break;
            case 3 * TURN_RIGHT_ANGLE:
                coord = new Coordinate(coord.x - 1, coord.y);
                break;
        }
    }

    public boolean isDetouring() {
        return coordBeforeDetour != null;
    }

    public boolean isDetourFinished() {
        if (coordBeforeDetour != null) {
            Coordinate coord = getRobotCoordinate();
            if (pilotDirection == directionBeforeDetour) {
                Coordinate frontCell = frontCellOf(coord);
                return frontCell == null || hasDetected(frontCell); //circumstance 1: after detouring
            }
            boolean isSameLine =
                    directionBeforeDetour == 0 && coord.x == coordBeforeDetour.x && coord.y > coordBeforeDetour.y
                            || directionBeforeDetour == 2 * TURN_RIGHT_ANGLE && coord.x == coordBeforeDetour.x
                            && coord.y < coordBeforeDetour.y
                            || directionBeforeDetour == TURN_RIGHT_ANGLE && coord.x > coordBeforeDetour.x
                            && coord.y == coordBeforeDetour.y
                            || directionBeforeDetour == 3 * TURN_RIGHT_ANGLE && coord.x < coordBeforeDetour.x
                            && coord.y == coordBeforeDetour.y;
            return isSameLine && hasObstacleFront() && Math.abs(directionBeforeDetour - pilotDirection) == 180; //circumstance 2: after detouring
        }
        return true;
    }

    public void detourFinished() {

        Coordinate coord = getRobotCoordinate();
        if ((pilotDirection + TURN_RIGHT_ANGLE) % 360 == directionBeforeDetour) { //circumstance 1: update cell values after detouring
            switch (directionBeforeDetour) {
                case 0:
                    for (int i = coordBeforeDetour.y + 1; i < coord.y; i++) {
                        Coordinate c = new Coordinate(coord.x, i);
                        if (!hasDetected(c)) {
                            saveDetectedResult(c, 1);
                        }
                    }
                    break;
                case TURN_RIGHT_ANGLE:
                    for (int i = coordBeforeDetour.x + 1; i < coord.x; i++) {
                        Coordinate c = new Coordinate(i, coord.y);
                        if (!hasDetected(c)) {
                            saveDetectedResult(c, 1);
                        }
                    }
                    break;
                case 2 * TURN_RIGHT_ANGLE:
                    for (int i = coordBeforeDetour.y - 1; i > coord.y; i--) {
                        Coordinate c = new Coordinate(coord.x, i);
                        if (!hasDetected(c)) {
                            saveDetectedResult(c, 1);
                        }
                    }
                    break;
                case 3 * TURN_RIGHT_ANGLE:
                    for (int i = coordBeforeDetour.x - 1; i > coord.x; i--) {
                        Coordinate c = new Coordinate(i, coord.y);
                        if (!hasDetected(c)) {
                            saveDetectedResult(c, 1);
                        }
                    }
                    break;
            }
        } else { //circumstance 2: update cell values after detouring
            switch (directionBeforeDetour) {
                case 0:
                    for (int i = coordBeforeDetour.y + 1; i < coord.y; i++) {
                        Coordinate c = new Coordinate(coord.x, i);
                        if (!hasDetected(c)) {
                            saveDetectedResult(c, 1);
                        }
                    }
                    for (int i = coordBeforeDetour.x; i < coord.x; i++) {
                        Coordinate c = new Coordinate(i, coord.y);
                        if (!hasDetected(c)) {
                            saveDetectedResult(c, 1);
                        }
                    }
                    break;
                case TURN_RIGHT_ANGLE:
                    for (int i = coordBeforeDetour.x + 1; i < coord.x; i++) {
                        Coordinate c = new Coordinate(i, coord.y);
                        if (!hasDetected(c)) {
                            saveDetectedResult(c, 1);
                        }
                    }
                    for (int i = coordBeforeDetour.y; i > coord.y; i--) {
                        Coordinate c = new Coordinate(coord.x, i);
                        if (!hasDetected(c)) {
                            saveDetectedResult(c, 1);
                        }
                    }
                    break;
                case 2 * TURN_RIGHT_ANGLE:
                    for (int i = coordBeforeDetour.y - 1; i > coord.y; i--) {
                        Coordinate c = new Coordinate(coord.x, i);
                        if (!hasDetected(c)) {
                            saveDetectedResult(c, 1);
                        }
                    }
                    for (int i = coordBeforeDetour.x; i > coord.x; i--) {
                        Coordinate c = new Coordinate(i, coord.y);
                        if (!hasDetected(c)) {
                            saveDetectedResult(c, 1);
                        }
                    }
                    break;
                case 3 * TURN_RIGHT_ANGLE:
                    for (int i = coordBeforeDetour.x - 1; i > coord.x; i--) {
                        Coordinate c = new Coordinate(i, coord.y);
                        if (!hasDetected(c)) {
                            saveDetectedResult(c, 1);
                        }
                    }
                    for (int i = coordBeforeDetour.y; i < coord.y; i++) {
                        Coordinate c = new Coordinate(coord.x, i);
                        if (!hasDetected(c)) {
                            saveDetectedResult(c, 1);
                        }
                    }
                    break;
            }
        }
        coordBeforeDetour = null;
        directionBeforeDetour = -1;
    }

    public void saveDetectedResult(Coordinate coord, int result) { //update cell values
        PilotMonitor.setArrayValue(coord.x, coord.y, result);
    }

    public Coordinate getRobotCoordinate() {
        return coord;
    }

    public boolean hasObstacleFront() {
        boolean res = me.getDistance() < CELL_SIZE / 100f;
        // System.out.println("hasObstacleFront:" + res);
        return res;
    }

    public boolean isDetectedObstacle(Coordinate coord) {
        return coord != null && PilotMonitor.getArrayValue(coord.x, coord.y) == 1;
    }

    public boolean hasDetected(Coordinate coord) {
        return coord != null && PilotMonitor.getArrayValue(coord.x, coord.y) != -1;
    }

    public void rotate(int angle) {
        pilot.rotate(angle);
        pilotDirection = (pilotDirection + angle) % 360;
        if (pilotDirection < 0) {
            pilotDirection += 360;
        }
    }

    Coordinate frontCellOf(Coordinate coord) { //no front cell || front cell's coordinate
        if (pilotDirection == 0) {
            return coord.y >= COL_COUNT - 1 ? null : new Coordinate(coord.x, coord.y + 1);
        } else if (pilotDirection == TURN_RIGHT_ANGLE) {
            return coord.x >= ROW_COUNT - 1 ? null : new Coordinate(coord.x + 1, coord.y);
        } else if (pilotDirection == 2 * TURN_RIGHT_ANGLE) {
            return coord.y <= 0 ? null : new Coordinate(coord.x, coord.y - 1);
        } else {
            return coord.x <= 0 ? null : new Coordinate(coord.x - 1, coord.y);
        }
    }

    Coordinate leftCellOf(Coordinate coord) { //no left cell || left cell's coordinate
        if (pilotDirection == TURN_RIGHT_ANGLE) {
            return coord.y >= COL_COUNT - 1 ? null : new Coordinate(coord.x, coord.y + 1);
        } else if (pilotDirection == 2 * TURN_RIGHT_ANGLE) {
            return coord.x >= ROW_COUNT - 1 ? null : new Coordinate(coord.x + 1, coord.y);
        } else if (pilotDirection == 3 * TURN_RIGHT_ANGLE) {
            return coord.y <= 0 ? null : new Coordinate(coord.x, coord.y - 1);
        } else {
            return coord.x <= 0 ? null : new Coordinate(coord.x - 1, coord.y);
        }
    }

    Coordinate rightCellOf(Coordinate coord) { //no right cell || right cell's coordinate
        if (pilotDirection == 3 * TURN_RIGHT_ANGLE) {
            return coord.y >= COL_COUNT - 1 ? null : new Coordinate(coord.x, coord.y + 1);
        } else if (pilotDirection == 0) {
            return coord.x >= ROW_COUNT - 1 ? null : new Coordinate(coord.x + 1, coord.y);
        } else if (pilotDirection == TURN_RIGHT_ANGLE) {
            return coord.y <= 0 ? null : new Coordinate(coord.x, coord.y - 1);
        } else {
            return coord.x <= 0 ? null : new Coordinate(coord.x - 1, coord.y);
        }
    }

    public boolean takeControl() {
        return true;
    }

    public void suppress() {
        suppressed = true;
    }

    class Coordinate {
        int x;
        int y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

}
