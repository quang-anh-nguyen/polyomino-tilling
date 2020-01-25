import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Arrays;

public class Sudoku {

	public LinkedList groundSet;
	LinkedList<Set> collection;

	static class Row {
		int r;

		public Row(int r) {
			this.r = r;
		}
	}

	static class Column {
		int c;

		private Column(int c) {
			this.c = c;
		}
	}

	class Box {
		int b;

		private Box(int b) {
			this.b = b;
		}
	}

	static Row[] rows;
	static Column[] columns;
	static Box[] boxes;

	static public Row getRow(int r) {
		if (rows == null) {
			rows = new Row[9];
			for (int i = 0; i < 9; i++)
				rows[i] = new Row(i+1);
		}
		return rows[r-1];
	}

	public Column getColumn(int c) {
		if (columns == null) {
			columns = new Column[9];
			for (int i = 0; i < 9; i++)
				columns[i] = new Column(i+1);
		}
		return columns[c-1];
	}

	public Box getBox(int b) {
		if (boxes == null) {
			boxes = new Box[9];
			for (int i = 0; i < 9; i++)
				boxes[i] = new Box(i+1);
		}
		return boxes[b-1];
	}

	int[][] question = new int[9][9];
	LinkedList<int[][]> solution = new LinkedList<int[][]>();

	public Sudoku(int[][] question) {
		this.question = question;
	}

	public void makeGroundSet() {
		groundSet = new LinkedList();
		for (int i = 1; i <= 9; i++)
			for (int j = 1; j <= 9; j++) {
				LinkedList s = new LinkedList();
				s.add(getRow(i));
				s.add(getColumn(j));
				groundSet.add(s);
			}
		for (int i = 1; i <= 9; i++)
			for (int j = 1; j <= 9; j++) {
				LinkedList s = new LinkedList();
				s.add(getRow(i));
				s.add(j);
				groundSet.add(s);
			}
		for (int i = 1; i <= 9; i++)
			for (int j = 1; j <= 9; j++) {
				LinkedList s = new LinkedList();
				s.add(getColumn(i));
				s.add(j);
				groundSet.add(s);
			}
		for (int i = 1; i <= 9; i++)
			for (int j = 1; j <= 9; j++) {
				LinkedList s = new LinkedList();
				s.add(getBox(i));
				s.add(j);
				groundSet.add(s);
			}
	}

	class possibility {
		Row r;
		Column c;
		int num;

		public possibility(Row r, Column c, int num) {
			this.r = r;
			this.c = c;
			this.num = num;
		}

		public possibility(dataObj o) {
			dataObj cur = o;
			while (true) {
				LinkedList cc = (LinkedList) cur.C.N;
				if (cc.get(1) instanceof Column) {
					this.r = (Row) cc.get(0);
					this.c = (Column) cc.get(1);
					this.num = (int) ((LinkedList) cur.R.C.N).get(1);
					break;
				} else
					cur = cur.R;
			}
		}

		public Set satisfy() {
			Set s = new HashSet();
			int b = (r.r - 1) / 3 * 3 + (c.c - 1) / 3 + 1;
			s.add(groundSet.get((r.r - 1) * 9 + c.c - 1));
			s.add(groundSet.get(81 + (r.r - 1) * 9 + num - 1));
			s.add(groundSet.get(81 + 81 + (c.c - 1) * 9 + num - 1));
			s.add(groundSet.get(81 + 81 + 81 + (b - 1) * 9 + num - 1));
			return s;
		}

	}

	public void makeCollection() {
		collection = new LinkedList<Set>();
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++) {
				if (question[i][j] > 0)
					collection.add((new possibility(getRow(i + 1), getColumn(j + 1), question[i][j]).satisfy()));
				else
					for (int k = 1; k <= 9; k++)
						collection.add((new possibility(getRow(i + 1), getColumn(j + 1), k).satisfy()));
			}
	}

	public void solve() {
		makeGroundSet();
		makeCollection();
		DancingLinks<Object> DCL = new DancingLinks(groundSet, collection);
		DCL.exactCover();
		int k = 0;
		for (Set<dataObj> sol : DCL.solution) {
			int[][] s = new int[9][9];
			for (dataObj o : sol) {
				possibility move = new possibility(o);
				s[move.r.r - 1][move.c.c - 1] = move.num;
			}
			solution.add(s);
		}
	}

	public Sudoku(String s) {
		int k = 0;
		for (int i = 0; i < 9; i++)
			for (int j = 0; j < 9; j++) {
				question[i][j] = Character.getNumericValue((s.charAt(k)));
				k++;
			}
	}
	
	public static String drawTable(int[][] table) {
		String s = "";
		for (int i =0; i<9; i++) {
			for (int j=0;j<9;j++) {
				s += table[i][j] + " ";
			}
			s+= "\n";
		}
		s+= "\n";
		return s;
	}
	
	public String toString() {
		String s = "";
		s += drawTable(question);
		for (int[][] u : solution)
			s+= drawTable(u);
		return s;
	}

	public static void main(String[] args) {
		Sudoku SDK = new Sudoku("004300209005009001070060043006002087190007400050083000600000105003508690042910300");
		SDK.solve();
		System.out.println(SDK);
	}

}
