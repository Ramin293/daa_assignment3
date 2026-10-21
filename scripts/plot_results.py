"""Render benchmark timing plots from results/results.csv."""

import argparse
import csv
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.ticker import FuncFormatter


SERIES = (
    ("Kruskal", "undirected", "Kruskal", "#315c99", "o", "-", 0),
    ("Prim", "undirected", "Prim", "#d77b30", "s", "-", 0),
    ("Topological", "dag", "Topological (DAG)", "#188a79", "^", "-", 1),
    ("Topological", "cyclic", "Topological (cycle)", "#188a79", "x", "--", 1),
    ("SCC", "dag", "SCC (DAG)", "#8056a6", "D", "-", 1),
    ("SCC", "cyclic", "SCC (cycle)", "#8056a6", "s", "--", 1),
    ("Dijkstra", "cyclic", "Dijkstra", "#bf4b4b", "o", "-", 1),
)


def read_results(path: Path) -> dict[tuple[str, str], list[dict]]:
    groups: dict[tuple[str, str], list[dict]] = {}
    with path.open(newline="", encoding="utf-8") as stream:
        for row in csv.DictReader(stream):
            key = (row["algorithm"], row["graph_type"])
            groups.setdefault(key, []).append(row)
    for algorithm, graph_type, *_ in SERIES:
        rows = groups.get((algorithm, graph_type), [])
        if len(rows) != 3 or {int(row["vertices"]) for row in rows} != {10, 100, 1000}:
            raise ValueError(f"Expected three graph sizes for {algorithm}/{graph_type}")
        if any(int(row["repetitions"]) != 5 for row in rows):
            raise ValueError("Every plotted timing must average five measured runs")
    return groups


def render(groups: dict, x_field: str, output: Path, preview: Path | None) -> None:
    plt.rcParams.update({
        "font.family": "DejaVu Sans",
        "font.size": 10,
        "svg.fonttype": "none",
        "svg.hashsalt": "daa-assignment3",
    })
    figure, axes = plt.subplots(1, 2, figsize=(13, 5), constrained_layout=True)
    figure.suptitle("Smart city graph algorithms: measured execution time\n"
                     "Seed 42 · mean of 5 measured runs", fontsize=14, fontweight="bold")
    ticks = [10, 100, 1000] if x_field == "vertices" else [30, 500, 5000]
    for ax, title in zip(axes, ("Minimum spanning tree", "Directed graph analysis and routes")):
        ax.set_title(title, fontsize=12, fontweight="bold", pad=12)
        ax.set_xscale("log")
        ax.set_xticks(ticks)
        ax.xaxis.set_major_formatter(FuncFormatter(lambda value, _: f"{value:,.0f}"))
        ax.set_xlim(ticks[0] / 1.4, ticks[-1] * 1.4)
        ax.set_xlabel(f"Number of {x_field} (log scale)")
        ax.set_ylabel("Mean execution time (ms)")
        ax.grid(axis="y", color="#d7dde5", linewidth=0.8)
        ax.spines[["top", "right"]].set_visible(False)

    for algorithm, graph_type, label, color, marker, line, panel in SERIES:
        rows = sorted(groups[(algorithm, graph_type)], key=lambda row: int(row[x_field]))
        x = [int(row[x_field]) for row in rows]
        y = [float(row["mean_time_ns"]) / 1_000_000 for row in rows]
        axes[panel].plot(x, y, label=label, color=color, marker=marker,
                         linestyle=line, linewidth=2, markersize=6)

    axes[0].legend(frameon=False, loc="upper left")
    axes[1].legend(frameon=False, loc="upper left", fontsize=9)
    for ax in axes:
        ax.set_ylim(bottom=0)
    output.parent.mkdir(parents=True, exist_ok=True)
    figure.savefig(output, metadata={"Date": None, "Creator": "DAA Assignment 3"})
    svg = output.read_text(encoding="utf-8")
    output.write_text("\n".join(line.rstrip() for line in svg.splitlines()) + "\n",
                      encoding="utf-8")
    if preview is not None:
        preview.parent.mkdir(parents=True, exist_ok=True)
        figure.savefig(preview, dpi=150)
    plt.close(figure)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--csv", type=Path, default=Path("results/results.csv"))
    parser.add_argument("--output-dir", type=Path, default=Path("results/plots"))
    parser.add_argument("--preview-dir", type=Path)
    args = parser.parse_args()
    groups = read_results(args.csv)
    for field in ("vertices", "edges"):
        output = args.output_dir / f"time_vs_{field}.svg"
        preview = args.preview_dir / f"time_vs_{field}.png" if args.preview_dir else None
        render(groups, field, output, preview)
        print(f"Wrote {output}")


if __name__ == "__main__":
    main()
