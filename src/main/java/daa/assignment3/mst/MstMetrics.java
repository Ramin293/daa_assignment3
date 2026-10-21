package daa.assignment3.mst;

/** Counts algorithm work without including graph construction. */
public record MstMetrics(
        long edgeExaminations,
        long orderingComparisons,
        long queueOffers,
        long queuePolls,
        long dsuFindCalls,
        long dsuParentTraversals,
        long dsuUnionCalls) {}
