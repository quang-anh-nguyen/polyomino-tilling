import java.util.LinkedList;

//  This class serves as enumeration of fixed polyominoes using the algorithm of Redelmeier

public class PolyNode extends Polyomino {

	static PolyNode tree; // storing all considered configurations of fixed polys
	static int N;

	public LinkedList<PolyNode> children;

	public PolyNode() {
		super();
		this.children = new LinkedList<PolyNode>();
		this.untried = (new Polyomino("[(0,0)]")).squares;
		this.added = new LinkedList<Square>();
	}

//	public PolyNode(Polyomino data) {
//		t
//		this.data = data;
//		this.children = new LinkedList<PolyNode>();
////		this.parent = null;
//		this.untried = new LinkedList<Square>();
//		this.added = new LinkedList<Square>();
//	}

//	public PolyNode getParent() {
//		return this.parent;
//	}
//
//	public void setParent(PolyNode parent) {
//		this.parent = parent;
//	}

	public void addChild(PolyNode child) {
		this.children.add(child);
	}

	public void addChildren(LinkedList<PolyNode> children) {
		for (PolyNode child : children)
			this.addChild(child);
	}

	// check if a cell is not included in this's born son or its brothers or its
	// ancestors' older brothers
	// not accepting cells that violate the polyomino's base (leftmost bottom cell)

	public boolean checkCell(Square s) { // cell checked is already in this.neighbors()
		if ((s.y < 0))
			return false;
		if ((s.x <= 0) && (s.y == 0))
			return false;
		if (this.contains(s))
			return false;
		if (s.inList(this.untried) >= 0)
			return false;
		return true;
	}

	public LinkedList<Square> untried;
	public LinkedList<Square> added;

	public static LinkedList<PolyNode> listN = new LinkedList<PolyNode>();

	@Override
	public PolyNode addSquare(Square sq) {
		if (this.contains(sq))
			return this;
		LinkedList<Square> squares = new LinkedList<Square>();
		squares.addAll(this.squares);
		squares.add(sq);
		return new PolyNode(squares);
	}

	public PolyNode(LinkedList<Square> squares) {
		super(squares);
		this.children = new LinkedList<PolyNode>();
		this.untried = new LinkedList<Square>();
		this.added = new LinkedList<Square>();

	}

	public static void devBranching() {
		tree = new PolyNode();
		listN.add(tree);
		while ((listN.getFirst().n < N)) {
			PolyNode parent = listN.pop();
			while (!parent.untried.isEmpty()) {
				Square cell = parent.untried.pop();
				parent.added.add(cell);
				PolyNode child = parent.addSquare(cell);
				parent.addChild(child);
				listN.addLast(child);
				if (child.n < N) {
					LinkedList<Square> u = cell.neighbors();
					child.untried.addAll(parent.untried);
					child.added.addAll(parent.added);
					for (Square s : u) {
						if (child.checkCell(s))
							if (s.inList(child.added) < 0)
								child.untried.add(s);
					}
				}
			}
		}
	}

	// get leaves - biggest polyominos generated

	public static LinkedList<PolyNode> generateFixed() {
		tree = new PolyNode();
		devBranching();
		tree = new PolyNode();
		return listN;
	}

	public PolyNode goDownTree(Polyomino P) {
		P = P.translation(-P.base.x, -P.base.y);
		if (P.getNum() == this.n)
			return this;
		LinkedList<Square> list = new LinkedList<Square>();
		list.addAll(P.squares);
		for (PolyNode child : this.children) {
			Square s = child.squares.getLast();
			int k = s.inList(list);
			if (k >= 0) {
				list.remove(k);
				return child.goDownTree(P);
			}
		}
		return null;
	}

	public static LinkedList<PolyNode> generateOneSide() {
		tree = new PolyNode();
		devBranching();
		int i = 0;
		while (i < listN.size()) {
			LinkedList<Polyomino> variants = listN.get(i).variants(false);
			variants.pop();
			while (!variants.isEmpty()) {
				Polyomino v = variants.pop();
				PolyNode b = tree.goDownTree(v);
				listN.remove(b);

			}
			i++;
		}
		return (listN);
	}

	public static LinkedList<Polyomino> generateFree() {
		tree = new PolyNode();
		devBranching();
		LinkedList<Polyomino> list = new LinkedList<Polyomino>();
		while (!listN.isEmpty()) {
			Polyomino P = listN.pop();
			LinkedList<Polyomino> variants = P.variants(true);
			list.add(P);
			variants.pop();
			while (!variants.isEmpty()) {
				Polyomino v = variants.pop();
				PolyNode b = tree.goDownTree(v);
				listN.remove(b);
			}
		}
		return list;
	}
}
