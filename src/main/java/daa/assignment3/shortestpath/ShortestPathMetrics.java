package daa.assignment3.shortestpath;

public record ShortestPathMetrics(
        long edgeExaminations,
        long successfulRelaxations,
        long queueOffers,
        long queuePolls,
        long orderingComparisons) {}
