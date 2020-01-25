import java.awt.Point;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

public class Square {

	public static int MAX = 100;
	public static Square[][] source = new Square[2 * MAX][2 * MAX];

	public static void init() {
		if (source[0][0] != null)
			return;
		for (int i = 0; i < 2 * MAX; i++)
			for (int j = 0; j < 2 * MAX; j++)
				source[i][j] = new Square(i - MAX, j - MAX);
	}

	public int x; // x coordinate
	public int y; // y coordinate

	public Square(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public static Square getSquare(int i, int j) {
		return source[i + MAX][j + MAX];
	}

	public boolean equals(Square s) {
		return (this.x == s.x) && (this.y == s.y);
	}

	@Override
	public String toString() {
		return "(" + this.x + "," + this.y + ")";
	}

	public LinkedList<Square> neighbors() {
		LinkedList<Square> list = new LinkedList<Square>();
		list.add(new Square(this.x, this.y - 1));
		list.add(new Square(this.x - 1, this.y));
		list.add(new Square(this.x + 1, this.y));
		list.add(new Square(this.x, this.y + 1));
		return list;
	}

	public int inList(LinkedList<Square> squares) {
		int i = 0;
		for (Square sq : squares) {
			if (this.equals(sq))
				return i;
			i++;
		}
		return -1;
	}

	public static void main(String[] args) {
		init();
	}
}
