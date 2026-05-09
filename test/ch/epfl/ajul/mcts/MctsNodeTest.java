package ch.epfl.ajul.mcts;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MctsNodeTest {

    // --- Factory methods ---

    @Test
    void newRootHasGameCountOne() {
        var root = MctsNode.newRoot();
        assertEquals(1, root.gameCount());
    }

    @Test
    void newRootHasTotalPointsZero() {
        var root = MctsNode.newRoot();
        assertEquals(0, root.totalPoints());
    }

    @Test
    void newRootHasSentinelPkMove() {
        var root = MctsNode.newRoot();
        assertEquals(0x3FF, root.pkMove());
    }

    @Test
    void newRootHasNullChildren() {
        var root = MctsNode.newRoot();
        assertNull(root.childNodes);
    }

    @Test
    void newMoveNodeHasGameCountZero() {
        var node = MctsNode.newMoveNode(42);
        assertEquals(0, node.gameCount());
    }

    @Test
    void newMoveNodeHasTotalPointsZero() {
        var node = MctsNode.newMoveNode(42);
        assertEquals(0, node.totalPoints());
    }

    @Test
    void newMoveNodeHasCorrectPkMove() {
        for (int move = 0; move < 1024; move += 1) {
            var node = MctsNode.newMoveNode(move);
            assertEquals(move, node.pkMove());
        }
    }

    @Test
    void newMoveNodeHasNullChildren() {
        var node = MctsNode.newMoveNode(0);
        assertNull(node.childNodes);
    }

    // --- Accessors ---

    @Test
    void pkMoveReturnsOnly10Bits() {
        var node = MctsNode.newMoveNode(0b1111111111);
        assertEquals(0x3FF, node.pkMove());

        var node2 = MctsNode.newMoveNode(0);
        assertEquals(0, node2.pkMove());

        var node3 = MctsNode.newMoveNode(0b0101010101);
        assertEquals(0b0101010101, node3.pkMove());
    }

    @Test
    void averagePointsWorksAfterMultipleEvaluations() {
        var node = MctsNode.newMoveNode(0);
        node.registerEvaluation(10);
        node.registerEvaluation(20);
        node.registerEvaluation(30);
        assertEquals(20.0, node.averagePoints(), 1e-9);
    }

    @Test
    void averagePointsUsesFloatingPointDivision() {
        var node = MctsNode.newMoveNode(0);
        node.registerEvaluation(10);
        node.registerEvaluation(20);
        node.registerEvaluation(30);
        node.registerEvaluation(41);
        // Sum = 101, count = 4, average = 25.25
        assertEquals(25.25, node.averagePoints(), 1e-9);
    }

    // --- registerEvaluation ---

    @Test
    void registerEvaluationIncrementsGameCount() {
        var node = MctsNode.newMoveNode(0);
        assertEquals(0, node.gameCount());
        node.registerEvaluation(5);
        assertEquals(1, node.gameCount());
        node.registerEvaluation(10);
        assertEquals(2, node.gameCount());
        node.registerEvaluation(15);
        assertEquals(3, node.gameCount());
    }

    @Test
    void registerEvaluationAddsTotalPoints() {
        var node = MctsNode.newMoveNode(0);
        assertEquals(0, node.totalPoints());
        node.registerEvaluation(5);
        assertEquals(5, node.totalPoints());
        node.registerEvaluation(10);
        assertEquals(15, node.totalPoints());
        node.registerEvaluation(15);
        assertEquals(30, node.totalPoints());
    }

    @Test
    void registerEvaluationDoesNotAffectPkMove() {
        var node = MctsNode.newMoveNode(123);
        node.registerEvaluation(1000);
        node.registerEvaluation(2000);
        assertEquals(123, node.pkMove());
    }

    @Test
    void registerEvaluationWorksWithZeroPoints() {
        var node = MctsNode.newMoveNode(0);
        node.registerEvaluation(0);
        assertEquals(1, node.gameCount());
        assertEquals(0, node.totalPoints());
        assertEquals(0.0, node.averagePoints(), 1e-9);
    }

    @Test
    void registerEvaluationWorksWithManySimulations() {
        var node = MctsNode.newMoveNode(0);
        int totalPoints = 0;
        for (int i = 0; i < 10000; i += 1) {
            int pts = i % 100;
            node.registerEvaluation(pts);
            totalPoints += pts;
        }
        assertEquals(10000, node.gameCount());
        assertEquals(totalPoints, node.totalPoints());
        assertEquals((double) totalPoints / 10000, node.averagePoints(), 1e-9);
    }

    // --- indexOfChildToExplore ---

    @Test
    void indexOfChildToExploreReturnsSequentialIndicesFirst() {
        var root = MctsNode.newRoot();
        root.childNodes = new MctsNode[5];
        for (int i = 0; i < 5; i += 1) {
            root.childNodes[i] = MctsNode.newMoveNode(i);
        }

        // Root starts with gameCount = 1, so first call returns 0
        assertEquals(0, root.indexOfChildToExplore());

        root.registerEvaluation(10); // gameCount = 2
        assertEquals(1, root.indexOfChildToExplore());

        root.registerEvaluation(10); // gameCount = 3
        assertEquals(2, root.indexOfChildToExplore());

        root.registerEvaluation(10); // gameCount = 4
        assertEquals(3, root.indexOfChildToExplore());

        root.registerEvaluation(10); // gameCount = 5
        assertEquals(4, root.indexOfChildToExplore());
    }

    @Test
    void indexOfChildToExploreReturnsUnexploredChildFirst() {
        var root = MctsNode.newRoot();
        root.childNodes = new MctsNode[3];
        for (int i = 0; i < 3; i += 1) {
            root.childNodes[i] = MctsNode.newMoveNode(i);
        }

        // After exploring all children sequentially, the next call should
        // use priority. But if a child has gameCount = 0, it returns that child.
        root.childNodes[0].registerEvaluation(50);
        root.childNodes[1].registerEvaluation(50);
        // child 2 still has gameCount = 0

        // We need gameCount > childNodes.length to trigger priority search
        for (int i = 0; i < 3; i += 1) root.registerEvaluation(0);
        // root gameCount = 4 > 3 children

        // child 2 has gameCount = 0 => should return index 2
        assertEquals(2, root.indexOfChildToExplore());
    }

    @Test
    void indexOfChildToExploreChoosesHighestPriority() {
        var root = MctsNode.newRoot();
        root.childNodes = new MctsNode[3];
        for (int i = 0; i < 3; i += 1) {
            root.childNodes[i] = MctsNode.newMoveNode(i);
        }

        // Give each child some evaluations
        root.childNodes[0].registerEvaluation(10);
        root.childNodes[1].registerEvaluation(100); // Highest average
        root.childNodes[2].registerEvaluation(5);

        // Set root gameCount > 3 (children count)
        for (int i = 0; i < 3; i += 1) root.registerEvaluation(0);
        // root.gameCount = 4

        int idx = root.indexOfChildToExplore();
        // Child 1 has highest average (100), should have highest priority
        assertEquals(1, idx);
    }

    @Test
    void indexOfChildToExplorePrefersUnexploredOverHighAverage() {
        var root = MctsNode.newRoot();
        root.childNodes = new MctsNode[2];
        root.childNodes[0] = MctsNode.newMoveNode(0);
        root.childNodes[1] = MctsNode.newMoveNode(1);

        root.childNodes[0].registerEvaluation(1000);
        // child 1 has gameCount 0

        root.registerEvaluation(0);
        root.registerEvaluation(0);
        // root gameCount = 3 > 2 children

        // child 1 has gameCount = 0 => infinite priority
        assertEquals(1, root.indexOfChildToExplore());
    }

    @Test
    void indexOfChildToExploreWithSingleChild() {
        var root = MctsNode.newRoot();
        root.childNodes = new MctsNode[1];
        root.childNodes[0] = MctsNode.newMoveNode(0);

        // gameCount = 1 <= 1 child, so returns 1 - 1 = 0
        assertEquals(0, root.indexOfChildToExplore());
    }

    @Test
    void indexOfChildToExploreThrowsWhenNoChildren() {
        var root = MctsNode.newRoot();
        assertThrows(Exception.class, root::indexOfChildToExplore);
    }

    // --- Packing correctness ---

    @Test
    void moveAndGameCountPackedCorrectly() {
        var node = MctsNode.newMoveNode(0b1010101010);
        assertEquals(0b1010101010, node.pkMove());
        assertEquals(0, node.gameCount());

        node.registerEvaluation(5);
        assertEquals(0b1010101010, node.pkMove());
        assertEquals(1, node.gameCount());

        for (int i = 0; i < 99; i += 1) node.registerEvaluation(1);
        assertEquals(0b1010101010, node.pkMove());
        assertEquals(100, node.gameCount());
    }

    @Test
    void rootMoveDoesNotChangeWithEvaluations() {
        var root = MctsNode.newRoot();
        assertEquals(0x3FF, root.pkMove());
        for (int i = 0; i < 50; i += 1) root.registerEvaluation(i);
        assertEquals(0x3FF, root.pkMove());
        assertEquals(51, root.gameCount()); // 1 initial + 50
    }
}
