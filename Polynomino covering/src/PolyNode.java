import java.util.LinkedList;

//  This class serves as enumeration of fixed polyominos

public class PolyNode {

	static PolyNode tree; // storing all considered configurations of fixed polys
	static int n;

	public Polyomino data;
	public LinkedList<PolyNode> children;
//	public PolyNode parent;

	public PolyNode() {
		this.data = new Polyomino();
		this.children = new LinkedList<PolyNode>();
//		this.parent = null;
		this.untried = (new Polyomino("[(0,0)]")).squares;
		this.added = new LinkedList<Square>();
	}

	public PolyNode(Polyomino data) {
		this.data = data;
		this.children = new LinkedList<PolyNode>();
//		this.parent = null;
		this.untried = new LinkedList<Square>();
		this.added = new LinkedList<Square>();
	}

	public void setData(Polyomino data) {
		this.data = data;
	}

	public Polyomino getData() {
		return this.data;
	}

//	public PolyNode getParent() {
//		return this.parent;
//	}
//
//	public void setParent(PolyNode parent) {
//		this.parent = parent;
//	}

	public LinkedList<PolyNode> getChildren() {
		return this.children;
	}

	public void addChild(PolyNode child) {
//		child.setParent(this);
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
		if (this.data.contains(s))
			return false;
		if (s.inList(this.untried) >= 0)
			return false;
		return true;
	}

	public LinkedList<Square> untried;
	public LinkedList<Square> added;

	public static LinkedList<PolyNode> DEV = new LinkedList<PolyNode>();

	public static void devBranching() {
		tree = new PolyNode();
		DEV.add(tree);
		while ((DEV.getFirst().data.getNum() < n)) {
			PolyNode parent = DEV.pop();
			while (!parent.untried.isEmpty()) {
				Square cell = parent.untried.pop();
				parent.added.add(cell);
				PolyNode child = new PolyNode(parent.data.addSquare(cell));
				parent.addChild(child);
				DEV.addLast(child);
				if (child.data.getNum() < n) {
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

	public LinkedList<PolyNode> extractLeaves() {
		LinkedList<PolyNode> list = new LinkedList<PolyNode>();
		if (this.data.getNum() == n) {
			list.add(this);
			return list;
		}
		for (PolyNode pn : this.getChildren()) {
			list.addAll(pn.extractLeaves());
		}
		return list;
	}

// 	extraction

	public static LinkedList<Polyomino> extractPolys(LinkedList<PolyNode> nodes) {
		LinkedList<Polyomino> list = new LinkedList<Polyomino>();
		for (PolyNode leave : nodes)
			list.add(leave.data);
		return list;
	}

	public static LinkedList<Polyomino> generateFixed() {
		tree = new PolyNode();
		devBranching();
		tree = new PolyNode();
//		return extractPolys(tree.extractLeaves())
		return extractPolys(DEV);
	}

	public PolyNode goDownTree(Polyomino P) {
		P = P.translation(-P.base.x, -P.base.y);
		if (P.getNum() == this.data.getNum())
			return this;
		LinkedList<Square> list = new LinkedList<Square>();
		list.addAll(P.squares);
		for (PolyNode child : this.getChildren()) {
			Square s = child.data.squares.getLast();
			int k = s.inList(list);
			if (k >= 0) {
				list.remove(k);
				return child.goDownTree(P);
			}
		}
		return null;
	}

	public static LinkedList<Polyomino> generateOneSide() {
		tree = new PolyNode();
		devBranching();
		int i = 0;
		while (i < DEV.size()) {
			LinkedList<Polyomino> variants = DEV.get(i).data.variants(false);
			variants.pop();
			while (!variants.isEmpty()) {
				Polyomino v = variants.pop();
				PolyNode b = tree.goDownTree(v);
				DEV.remove(b);

			}
			i++;
		}
		return extractPolys(DEV);
	}

	public static LinkedList<Polyomino> generateFree() {
		tree = new PolyNode();
		devBranching();
		LinkedList<Polyomino> list = new LinkedList<Polyomino>();
		while (!DEV.isEmpty()) {
			Polyomino P = DEV.pop().data;
			LinkedList<Polyomino> variants = P.variants(true);
			list.add(P);
			variants.pop();
			while (!variants.isEmpty()) {
				Polyomino v = variants.pop();
				PolyNode b = tree.goDownTree(v);
				DEV.remove(b);
				}
		}
		return list;
	}
}
