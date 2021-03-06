import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.opengl.*; 
import peasy.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class ChromoViz extends PApplet {




//for nice image panning, rotation and zoom
PeasyCam cam;

//for nice colors
ColorBrewer brewer = new ColorBrewer();

// float[] angles;



//fraction of window the full radius occupies 
int full_radius;

//radians to space chromosomes
float spacer_rad = 0.02f;

// int num_chr;


ArrayList<Genome> genomes = new ArrayList<Genome>();

Genome genome;



ArrayList colors = brewer.get_Set1_Qualitative_4();

int shift = 200;

int annot_alpha_val = 100;

boolean spread = true;

boolean stacked = false;

float chr_width;

float zoom = 1;


public void setup(){

  int min_size = min(displayWidth, displayHeight);
  size(min_size,min_size, P3D);
  // if (frame != null) {
  //   frame.setResizable(true);
  // }


  selectInput("Select a config file describing your genomes and annotations:", "parseConfigFile");


  	// parseConfigFile("test_config.txt");
  

	cam = new PeasyCam(this, width/2, height/2, 0, 300);
	// cam.setMinimumDistance(50);
  	// cam.setMaximumDistance(500);

	// Genome genome1 = new Genome("hg19.seqs.chr1-22.X.Y.fa.tsv");

	// genome1.addBed("test.bed", "dot", color(215,25,28), annot_alpha_val);

	// genome1.addBedPE("test.bedpe", color(166,217,106), annot_alpha_val);

 //  genome = genome1;

 //  Genome genome2 = new Genome("hg19.seqs.chr1-22.X.Y.fa.tsv");

 //  genome2.addBed("test.bed", "dot", color(215,25,28), annot_alpha_val);

 //  genome2.addBedPE("test2.bedpe", color(166,217,106), annot_alpha_val);


 //  genomes.add(genome1);
 //  genomes.add(genome2);
 //  genomes.add(genome1);
 //  genomes.add(genome2);


  // println(genomes);

	
	frameRate(30);
	// noLoop();

}

public void draw(){


	background(255);
	full_radius = PApplet.parseInt(min(width, height) * 0.4f);


	int counter = 0;


	//draw stacked  
	if (stacked) {
		chr_width = 40;
		for (Genome genome : genomes){

			pushMatrix();
			scale(zoom);  
			translate(0, 0, counter * shift);
			genome.draw(full_radius, width/2, width/2, chr_width);
			counter ++;
			popMatrix();

		}
	}
	// println(counter);


	//draw spread
	if (spread){

		float sqrt_num_chr = sqrt(genomes.size());
		int numcols =  round(sqrt_num_chr);
		int numrows = ceil(sqrt_num_chr);

		int col_spacer = width / (numcols + 1);
		int row_spacer = height / (numrows + 1);

		int col_index = 0;
		int row_index = 0;

		int x_pos = 0;
		int y_pos = 0;

		int radius = PApplet.parseInt(( width / (max(numcols, numrows) + 1) ) * 0.4f);

		int chr_width = 10;

		for (Genome genome : genomes){

			pushMatrix();
			scale(zoom);  
			translate(x_pos + col_index * col_spacer, y_pos + row_index * row_spacer, 0);
			genome.draw(radius, col_spacer/2 , col_spacer/2, chr_width);
			popMatrix();
			//update column and row indices to draw row by row
			if (col_index < numcols){
				col_index ++;
			}
			else{
				row_index ++;
				col_index = 0;
			}
		}
	}

	// scale(zoom);  

}

public void parseConfigFile(File selection) {
	if (selection == null) {
		println("Window was closed or the user hit cancel.");
	} 

	else {
		// println("load config file: " + selection.getAbsolutePath());

		String lines[] = loadStrings(selection.getAbsolutePath());

		Genome current_genome = null;
		for (String current_line : lines){
			String tokens[] = split(current_line, TAB);
			// println(tokens[0]);

			//genome line is of the form
			//genome	filename	samplename
			if (tokens[0].equals("genome")){
				if(current_genome != null){
					genomes.add(current_genome);
				}
				current_genome = new Genome(tokens[1], tokens[2]);
				// println("add genome");
			}

			//bed line is of the form
			//bed	filename	r,g,b	[dot|interval]
			else if (tokens[0].equals("bed")){
				current_genome.addBed(tokens[1], parseColor(tokens[2]), tokens[3], annot_alpha_val);
				// println("add bed");
			}

			//bedpe line is of the form
			//bedpe	filename	r,g,b
			else if (tokens[0].equals("bedpe")){
				current_genome.addBedPE(tokens[1], parseColor(tokens[2]), annot_alpha_val);
				// println("add bedpe");
			}

		}
		genomes.add(current_genome);
		genome = current_genome;

		// wait = false;
	}
}

public int parseColor(String s){
	String[] rgb = split(s, ",");
	int r = Integer.parseInt(rgb[0]);
	int g = Integer.parseInt(rgb[1]);
	int b = Integer.parseInt(rgb[2]);

	return color(r,g,b);

}
// }

public void mouseEntered(MouseEvent e) {
  loop();
}

public void mouseExited(MouseEvent e) {
  noLoop();
}

public void keyPressed() {
  if (key == 'o') {
    zoom += 0.1f;
  } else if (key == 'i'){
    zoom -= 0.1f;
  }
  else if (key == 'f'){
    spread = true;
    stacked = false;
  }
  else if (key == 's'){
    spread = false;
    stacked = true;
    shift = 0;

  }
  else if (key == '+'){
  	shift += 10;
  }
  else if (key == '-'){
  	shift -= 10;
  }

}









//class to store a bed file (bed3)
//methods to draw it in different ways


class BedAnnot{
	
	Table bed_table;
	String glyph;
	int col;
	int alpha_val;

	BedAnnot(String bed_file, String g, int c, int a){

		bed_table = loadTable(bed_file, "header, tsv");
		glyph = g;
		alpha_val = a;
		col = c;
		println("tbale", bed_file, bed_table);

	}

	public void draw(float radius, float x, float y){
		if(	glyph.equals("dot")){
			drawAsDot(radius, x, y);
		}
		else if(glyph.equals("interval")){
			drawAsInt(radius, x, y);
		}
		else {
			println("ERROR: wrong glyph type for bed: ", glyph);
		}
	}


	//draw as colored interval
	public void drawAsInt(float radius, float x, float y){

		float start_angle = 0.0f; 
		float end_angle = 0.0f; 
		String chr_name = "";
		// int startpos = 0;
		// int endpos = 0;

		for (TableRow row : bed_table.rows()){

			chr_name = row.getString("chr");
			// startpos = row.getInt("start");
			// endpos = row.getInt("end");

			// println(chr_name, startpos, endpos);

			start_angle = genome.genToPolar(chr_name, row.getInt("start"));
			end_angle = genome.genToPolar(chr_name, row.getInt("end"));
			intBand(start_angle, end_angle, radius, x, y, 40, col, alpha_val);
		}

	}

	//draw as dot in the center of the interval
	public void drawAsDot(float radius, float x, float y){

		float start_angle = 0.0f; 
		float end_angle = 0.0f; 
		String chr_name = "";
		// int startpos = 0;
		// int endpos = 0;

		for (TableRow row : bed_table.rows()){

			chr_name = row.getString("chr");
			// startpos = row.getInt("start");
			// endpos = row.getInt("end");

			// println(chr_name, startpos, endpos);

			start_angle = genome.genToPolar(chr_name, row.getInt("start"));
			end_angle = genome.genToPolar(chr_name, row.getInt("end"));
			intMidDot(start_angle, end_angle, radius, x, y, col, alpha_val);
		}

	}


	//draw as triangle

	//draw as scatter?

	//draw as line
}
class BedPEAnnot{
	
	Table bed_pe_table;

	int c;

	int alpha_val;


	BedPEAnnot(String bed_file, int col, int a){

		c = col;
		alpha_val = a;

		bed_pe_table = loadTable(bed_file, "header, tsv");
	}

	public void drawAsIntPairBezier(float radius, float x, float y, float chr_width){

		float start_angle1 = 0.0f; 
		float end_angle1 = 0.0f; 
		String chr_name1 = "";

		float start_angle2 = 0.0f; 
		float end_angle2 = 0.0f; 
		String chr_name2 = "";



		for (TableRow row : bed_pe_table.rows()){

			chr_name1 = row.getString("chr1");
			start_angle1 = genome.genToPolar(chr_name1, row.getInt("start1"));
			end_angle1 = genome.genToPolar(chr_name1, row.getInt("end1"));

			chr_name2 = row.getString("chr2");
			start_angle2 = genome.genToPolar(chr_name2, row.getInt("start2"));
			end_angle2 = genome.genToPolar(chr_name2, row.getInt("end2"));


			intPairBezier(start_angle1, end_angle1, start_angle2, end_angle2, radius - chr_width/2, x, y, c, alpha_val);

		}

	}

	public void draw(float radius, float x, float y, float chr_width){
		drawAsIntPairBezier(radius, x, y, chr_width);

		//maybe have other ways of drawing this later?
	}
}
class ChromIdeogram{


	Table chr_table;
	float tot_length;
	int num_chr;

	

	ChromIdeogram(String table_name){

		chr_table = loadTable(table_name, "header, tsv");

		num_chr = chr_table.getRowCount();
		println(num_chr);


		//take into account the spacers between chromosomes 
		float tot_available_angle = TWO_PI - (num_chr*spacer_rad);

		// angles = new float[chr_table.getRowCount()];



		//add columns to table to store info for polar coordinates

		//to store the start angle for polar coordinates of this chr
		chr_table.addColumn("start_angle", Table.FLOAT);
		//to store the end angle for polar coordinates of this chr
		chr_table.addColumn("end_angle", Table.FLOAT);
		//to store int value of chr length
		chr_table.addColumn("chr_length_bp", Table.INT);

		
		tot_length = 0.0f;
		for (TableRow row : chr_table.rows()){
			tot_length += row.getInt("length");
		}

		float start_angle = 0;

		int index = 0;

		for (TableRow row : chr_table.rows()){
			
			int chr_length = row.getInt("length");
			String chr_name = row.getString("chr_name");
			
			float angle = map(chr_length, 0, tot_length, 0.0f, tot_available_angle);

			row.setFloat("start_angle", start_angle);
			row.setFloat("end_angle", start_angle + angle);

			start_angle = start_angle + angle + spacer_rad;
			println(index, chr_name);
			index ++;

		}
	} 

	public void draw(float radius, float x, float y, float chr_width){

		int i = 0;
		for (TableRow row : chr_table.rows()){
			int gray_val = PApplet.parseInt(map(i, 0, num_chr, 0, 255));
			intBand(row.getFloat("start_angle"), row.getFloat("end_angle"), radius, x, y, chr_width, gray_val, 255);
			i++;
		}
	}

	public TableRow get_chr_table_row(String chr_name){

		// return chr_table.getRow(getChrIndex(chr_name));
		return chr_table.findRow(chr_name, "chr_name");

	}

	// int getChrIndex(String chr_name){
	// 	println("chr_name", chr_name);
	// 	if (chr_name.equals( "X")){
	// 		return 22;
	// 	}
	// 	else if (chr_name.equals("Y")){
	// 			return 23;
	// 	}
	// 	else if (chr_name.equals("pb-ef1-neo_seq")){
	// 		return 24;
			
	// 	} 
	// 	else{

	// 		// println(chr_name, int(chr_name));
	// 		return Integer.parseInt(chr_name) - 1;
	// 	}
	// }

	public float genToPolar(String chr_name, int pos){

		// println(chr_name, pos);
		// TableRow chr_ref = chr_table.getRow(getChrIndex(chr_name));
		TableRow chr_ref = chr_table.findRow(chr_name, "chr_name");

		if (chr_ref == null){
			println("ERROR in trying to retrieve row for " + chr_name + " in table, this name does not exist. Are you sure the chr names match between your annotations and your genome file?");
			exit();
		}

		// find the angle corresponding to the chromosome and position, given the start and end angles defined for that chromosome
		float angle = map(pos, 0, chr_ref.getInt("length"), chr_ref.getFloat("start_angle"), chr_ref.getFloat("end_angle"));
		// println(angle);
		return angle;
	}
}
//
//
// Generated Class
// (2012-09-20 15:29:49.747163)
class ColorBrewer{
    
// color Pastel2_Qualitative_7
public ArrayList get_Pastel2_Qualitative_7() {
  ArrayList Pastel2_Qualitative_7_ = new ArrayList();
  Pastel2_Qualitative_7_.add( color(179, 226, 205));
  Pastel2_Qualitative_7_.add(color(253, 205, 172));
  Pastel2_Qualitative_7_.add(color(203, 213, 232));
  Pastel2_Qualitative_7_.add(color(244, 202, 228));
  Pastel2_Qualitative_7_.add(color(230, 245, 201));
  Pastel2_Qualitative_7_.add(color(255, 242, 174));
  Pastel2_Qualitative_7_.add(color(241, 226, 204));
  return Pastel2_Qualitative_7_;
}


// color Pastel2_Qualitative_6
public ArrayList get_Pastel2_Qualitative_6() {
  ArrayList Pastel2_Qualitative_6_ = new ArrayList();
  Pastel2_Qualitative_6_.add(color(179, 226, 205));
  Pastel2_Qualitative_6_.add(color(253, 205, 172));
  Pastel2_Qualitative_6_.add(color(203, 213, 232));
  Pastel2_Qualitative_6_.add(color(244, 202, 228));
  Pastel2_Qualitative_6_.add(color(230, 245, 201));
  Pastel2_Qualitative_6_.add(color(255, 242, 174));
  return Pastel2_Qualitative_6_;
}


// color Pastel2_Qualitative_5
public ArrayList get_Pastel2_Qualitative_5() {
  ArrayList Pastel2_Qualitative_5_ = new ArrayList();
  Pastel2_Qualitative_5_.add(color(179, 226, 205));
  Pastel2_Qualitative_5_.add(color(253, 205, 172));
  Pastel2_Qualitative_5_.add(color(203, 213, 232));
  Pastel2_Qualitative_5_.add(color(244, 202, 228));
  Pastel2_Qualitative_5_.add(color(230, 245, 201));
  return Pastel2_Qualitative_5_;
}


// color Pastel2_Qualitative_4
public ArrayList get_Pastel2_Qualitative_4() {
  ArrayList Pastel2_Qualitative_4_ = new ArrayList();
  Pastel2_Qualitative_4_.add(color(179, 226, 205));
  Pastel2_Qualitative_4_.add(color(253, 205, 172));
  Pastel2_Qualitative_4_.add(color(203, 213, 232));
  Pastel2_Qualitative_4_.add(color(244, 202, 228));
  return Pastel2_Qualitative_4_;
}


// color Oranges_Sequential_8
public ArrayList get_Oranges_Sequential_8() {
  ArrayList Oranges_Sequential_8_ = new ArrayList();
  Oranges_Sequential_8_.add(color(255, 245, 235));
  Oranges_Sequential_8_.add(color(254, 230, 206));
  Oranges_Sequential_8_.add(color(253, 208, 162));
  Oranges_Sequential_8_.add(color(253, 174, 107));
  Oranges_Sequential_8_.add(color(253, 141, 60));
  Oranges_Sequential_8_.add(color(241, 105, 19));
  Oranges_Sequential_8_.add(color(217, 72, 1));
  Oranges_Sequential_8_.add(color(140, 45, 4));
  return Oranges_Sequential_8_;
}


// color Oranges_Sequential_9
public ArrayList get_Oranges_Sequential_9() {
  ArrayList Oranges_Sequential_9_ = new ArrayList();
  Oranges_Sequential_9_.add(color(255, 245, 235));
  Oranges_Sequential_9_.add(color(254, 230, 206));
  Oranges_Sequential_9_.add(color(253, 208, 162));
  Oranges_Sequential_9_.add(color(253, 174, 107));
  Oranges_Sequential_9_.add(color(253, 141, 60));
  Oranges_Sequential_9_.add(color(241, 105, 19));
  Oranges_Sequential_9_.add(color(217, 72, 1));
  Oranges_Sequential_9_.add(color(166, 54, 3));
  Oranges_Sequential_9_.add(color(127, 39, 4));
  return Oranges_Sequential_9_;
}


// color Set1_Qualitative_4
public ArrayList get_Set1_Qualitative_4() {
  ArrayList Set1_Qualitative_4_ = new ArrayList();
  Set1_Qualitative_4_.add(color(228, 26, 28));
  Set1_Qualitative_4_.add(color(55, 126, 184));
  Set1_Qualitative_4_.add(color(77, 175, 74));
  Set1_Qualitative_4_.add(color(152, 78, 163));
  return Set1_Qualitative_4_;
}


// color Set1_Qualitative_3
public ArrayList get_Set1_Qualitative_3() {
  ArrayList Set1_Qualitative_3_ = new ArrayList();
  Set1_Qualitative_3_.add(color(228, 26, 28));
  Set1_Qualitative_3_.add(color(55, 126, 184));
  Set1_Qualitative_3_.add(color(77, 175, 74));
  return Set1_Qualitative_3_;
}


// color Oranges_Sequential_4
public ArrayList get_Oranges_Sequential_4() {
  ArrayList Oranges_Sequential_4_ = new ArrayList();
  Oranges_Sequential_4_.add(color(254, 237, 222));
  Oranges_Sequential_4_.add(color(253, 190, 133));
  Oranges_Sequential_4_.add(color(253, 141, 60));
  Oranges_Sequential_4_.add(color(217, 71, 1));
  return Oranges_Sequential_4_;
}


// color Oranges_Sequential_5
public ArrayList get_Oranges_Sequential_5() {
  ArrayList Oranges_Sequential_5_ = new ArrayList();
  Oranges_Sequential_5_.add(color(254, 237, 222));
  Oranges_Sequential_5_.add(color(253, 190, 133));
  Oranges_Sequential_5_.add(color(253, 141, 60));
  Oranges_Sequential_5_.add(color(230, 85, 13));
  Oranges_Sequential_5_.add(color(166, 54, 3));
  return Oranges_Sequential_5_;
}


// color Oranges_Sequential_6
public ArrayList get_Oranges_Sequential_6() {
  ArrayList Oranges_Sequential_6_ = new ArrayList();
  Oranges_Sequential_6_.add(color(254, 237, 222));
  Oranges_Sequential_6_.add(color(253, 208, 162));
  Oranges_Sequential_6_.add(color(253, 174, 107));
  Oranges_Sequential_6_.add(color(253, 141, 60));
  Oranges_Sequential_6_.add(color(230, 85, 13));
  Oranges_Sequential_6_.add(color(166, 54, 3));
  return Oranges_Sequential_6_;
}


// color Oranges_Sequential_7
public ArrayList get_Oranges_Sequential_7() {
  ArrayList Oranges_Sequential_7_ = new ArrayList();
  Oranges_Sequential_7_.add(color(254, 237, 222));
  Oranges_Sequential_7_.add(color(253, 208, 162));
  Oranges_Sequential_7_.add(color(253, 174, 107));
  Oranges_Sequential_7_.add(color(253, 141, 60));
  Oranges_Sequential_7_.add(color(241, 105, 19));
  Oranges_Sequential_7_.add(color(217, 72, 1));
  Oranges_Sequential_7_.add(color(140, 45, 4));
  return Oranges_Sequential_7_;
}


// color Oranges_Sequential_3
public ArrayList get_Oranges_Sequential_3() {
  ArrayList Oranges_Sequential_3_ = new ArrayList();
  Oranges_Sequential_3_.add(color(254, 230, 206));
  Oranges_Sequential_3_.add(color(253, 174, 107));
  Oranges_Sequential_3_.add(color(230, 85, 13));
  return Oranges_Sequential_3_;
}


// color Purples_Sequential_4
public ArrayList get_Purples_Sequential_4() {
  ArrayList Purples_Sequential_4_ = new ArrayList();
  Purples_Sequential_4_.add(color(242, 240, 247));
  Purples_Sequential_4_.add(color(203, 201, 226));
  Purples_Sequential_4_.add(color(158, 154, 200));
  Purples_Sequential_4_.add(color(106, 81, 163));
  return Purples_Sequential_4_;
}


// color Set3_Qualitative_6
public ArrayList get_Set3_Qualitative_6() {
  ArrayList Set3_Qualitative_6_ = new ArrayList();
  Set3_Qualitative_6_.add(color(141, 211, 199));
  Set3_Qualitative_6_.add(color(255, 255, 179));
  Set3_Qualitative_6_.add(color(190, 186, 218));
  Set3_Qualitative_6_.add(color(251, 128, 114));
  Set3_Qualitative_6_.add(color(128, 177, 211));
  Set3_Qualitative_6_.add(color(253, 180, 98));
  return Set3_Qualitative_6_;
}


// color PRGn_Diverging_8
public ArrayList get_PRGn_Diverging_8() {
  ArrayList PRGn_Diverging_8_ = new ArrayList();
  PRGn_Diverging_8_.add(color(118, 42, 131));
  PRGn_Diverging_8_.add(color(153, 112, 171));
  PRGn_Diverging_8_.add(color(194, 165, 207));
  PRGn_Diverging_8_.add(color(231, 212, 232));
  PRGn_Diverging_8_.add(color(217, 240, 211));
  PRGn_Diverging_8_.add(color(166, 219, 160));
  PRGn_Diverging_8_.add(color(90, 174, 97));
  PRGn_Diverging_8_.add(color(27, 120, 55));
  return PRGn_Diverging_8_;
}


// color PRGn_Diverging_9
public ArrayList get_PRGn_Diverging_9() {
  ArrayList PRGn_Diverging_9_ = new ArrayList();
  PRGn_Diverging_9_.add(color(118, 42, 131));
  PRGn_Diverging_9_.add(color(153, 112, 171));
  PRGn_Diverging_9_.add(color(194, 165, 207));
  PRGn_Diverging_9_.add(color(231, 212, 232));
  PRGn_Diverging_9_.add(color(247, 247, 247));
  PRGn_Diverging_9_.add(color(217, 240, 211));
  PRGn_Diverging_9_.add(color(166, 219, 160));
  PRGn_Diverging_9_.add(color(90, 174, 97));
  PRGn_Diverging_9_.add(color(27, 120, 55));
  return PRGn_Diverging_9_;
}


// color RdYlBu_Diverging_4
public ArrayList get_RdYlBu_Diverging_4() {
  ArrayList RdYlBu_Diverging_4_ = new ArrayList();
  RdYlBu_Diverging_4_.add(color(215, 25, 28));
  RdYlBu_Diverging_4_.add(color(253, 174, 97));
  RdYlBu_Diverging_4_.add(color(171, 217, 233));
  RdYlBu_Diverging_4_.add(color(44, 123, 182));
  return RdYlBu_Diverging_4_;
}


// color PRGn_Diverging_4
public ArrayList get_PRGn_Diverging_4() {
  ArrayList PRGn_Diverging_4_ = new ArrayList();
  PRGn_Diverging_4_.add(color(123, 50, 148));
  PRGn_Diverging_4_.add(color(194, 165, 207));
  PRGn_Diverging_4_.add(color(166, 219, 160));
  PRGn_Diverging_4_.add(color(0, 136, 55));
  return PRGn_Diverging_4_;
}


// color Pastel2_Qualitative_3
public ArrayList get_Pastel2_Qualitative_3() {
  ArrayList Pastel2_Qualitative_3_ = new ArrayList();
  Pastel2_Qualitative_3_.add(color(179, 226, 205));
  Pastel2_Qualitative_3_.add(color(253, 205, 172));
  Pastel2_Qualitative_3_.add(color(203, 213, 232));
  return Pastel2_Qualitative_3_;
}


// color PRGn_Diverging_6
public ArrayList get_PRGn_Diverging_6() {
  ArrayList PRGn_Diverging_6_ = new ArrayList();
  PRGn_Diverging_6_.add(color(118, 42, 131));
  PRGn_Diverging_6_.add(color(175, 141, 195));
  PRGn_Diverging_6_.add(color(231, 212, 232));
  PRGn_Diverging_6_.add(color(217, 240, 211));
  PRGn_Diverging_6_.add(color(127, 191, 123));
  PRGn_Diverging_6_.add(color(27, 120, 55));
  return PRGn_Diverging_6_;
}


// color PRGn_Diverging_7
public ArrayList get_PRGn_Diverging_7() {
  ArrayList PRGn_Diverging_7_ = new ArrayList();
  PRGn_Diverging_7_.add(color(118, 42, 131));
  PRGn_Diverging_7_.add(color(175, 141, 195));
  PRGn_Diverging_7_.add(color(231, 212, 232));
  PRGn_Diverging_7_.add(color(247, 247, 247));
  PRGn_Diverging_7_.add(color(217, 240, 211));
  PRGn_Diverging_7_.add(color(127, 191, 123));
  PRGn_Diverging_7_.add(color(27, 120, 55));
  return PRGn_Diverging_7_;
}


// color RdYlBu_Diverging_7
public ArrayList get_RdYlBu_Diverging_7() {
  ArrayList RdYlBu_Diverging_7_ = new ArrayList();
  RdYlBu_Diverging_7_.add(color(215, 48, 39));
  RdYlBu_Diverging_7_.add(color(252, 141, 89));
  RdYlBu_Diverging_7_.add(color(254, 224, 144));
  RdYlBu_Diverging_7_.add(color(255, 255, 191));
  RdYlBu_Diverging_7_.add(color(224, 243, 248));
  RdYlBu_Diverging_7_.add(color(145, 191, 219));
  RdYlBu_Diverging_7_.add(color(69, 117, 180));
  return RdYlBu_Diverging_7_;
}


// color PRGn_Diverging_3
public ArrayList get_PRGn_Diverging_3() {
  ArrayList PRGn_Diverging_3_ = new ArrayList();
  PRGn_Diverging_3_.add(color(175, 141, 195));
  PRGn_Diverging_3_.add(color(247, 247, 247));
  PRGn_Diverging_3_.add(color(127, 191, 123));
  return PRGn_Diverging_3_;
}


// color Set2_Qualitative_5
public ArrayList get_Set2_Qualitative_5() {
  ArrayList Set2_Qualitative_5_ = new ArrayList();
  Set2_Qualitative_5_.add(color(102, 194, 165));
  Set2_Qualitative_5_.add(color(252, 141, 98));
  Set2_Qualitative_5_.add(color(141, 160, 203));
  Set2_Qualitative_5_.add(color(231, 138, 195));
  Set2_Qualitative_5_.add(color(166, 216, 84));
  return Set2_Qualitative_5_;
}


// color Set2_Qualitative_4
public ArrayList get_Set2_Qualitative_4() {
  ArrayList Set2_Qualitative_4_ = new ArrayList();
  Set2_Qualitative_4_.add(color(102, 194, 165));
  Set2_Qualitative_4_.add(color(252, 141, 98));
  Set2_Qualitative_4_.add(color(141, 160, 203));
  Set2_Qualitative_4_.add(color(231, 138, 195));
  return Set2_Qualitative_4_;
}


// color Set2_Qualitative_7
public ArrayList get_Set2_Qualitative_7() {
  ArrayList Set2_Qualitative_7_ = new ArrayList();
  Set2_Qualitative_7_.add(color(102, 194, 165));
  Set2_Qualitative_7_.add(color(252, 141, 98));
  Set2_Qualitative_7_.add(color(141, 160, 203));
  Set2_Qualitative_7_.add(color(231, 138, 195));
  Set2_Qualitative_7_.add(color(166, 216, 84));
  Set2_Qualitative_7_.add(color(255, 217, 47));
  Set2_Qualitative_7_.add(color(229, 196, 148));
  return Set2_Qualitative_7_;
}


// color Purples_Sequential_7
public ArrayList get_Purples_Sequential_7() {
  ArrayList Purples_Sequential_7_ = new ArrayList();
  Purples_Sequential_7_.add(color(242, 240, 247));
  Purples_Sequential_7_.add(color(218, 218, 235));
  Purples_Sequential_7_.add(color(188, 189, 220));
  Purples_Sequential_7_.add(color(158, 154, 200));
  Purples_Sequential_7_.add(color(128, 125, 186));
  Purples_Sequential_7_.add(color(106, 81, 163));
  Purples_Sequential_7_.add(color(74, 20, 134));
  return Purples_Sequential_7_;
}


// color YlGn_Sequential_4
public ArrayList get_YlGn_Sequential_4() {
  ArrayList YlGn_Sequential_4_ = new ArrayList();
  YlGn_Sequential_4_.add(color(255, 255, 204));
  YlGn_Sequential_4_.add(color(194, 230, 153));
  YlGn_Sequential_4_.add(color(120, 198, 121));
  YlGn_Sequential_4_.add(color(35, 132, 67));
  return YlGn_Sequential_4_;
}


// color Set2_Qualitative_3
public ArrayList get_Set2_Qualitative_3() {
  ArrayList Set2_Qualitative_3_ = new ArrayList();
  Set2_Qualitative_3_.add(color(102, 194, 165));
  Set2_Qualitative_3_.add(color(252, 141, 98));
  Set2_Qualitative_3_.add(color(141, 160, 203));
  return Set2_Qualitative_3_;
}


// color Set1_Qualitative_5
public ArrayList get_Set1_Qualitative_5() {
  ArrayList Set1_Qualitative_5_ = new ArrayList();
  Set1_Qualitative_5_.add(color(228, 26, 28));
  Set1_Qualitative_5_.add(color(55, 126, 184));
  Set1_Qualitative_5_.add(color(77, 175, 74));
  Set1_Qualitative_5_.add(color(152, 78, 163));
  Set1_Qualitative_5_.add(color(255, 127, 0));
  return Set1_Qualitative_5_;
}


// color Set2_Qualitative_8
public ArrayList get_Set2_Qualitative_8() {
  ArrayList Set2_Qualitative_8_ = new ArrayList();
  Set2_Qualitative_8_.add(color(102, 194, 165));
  Set2_Qualitative_8_.add(color(252, 141, 98));
  Set2_Qualitative_8_.add(color(141, 160, 203));
  Set2_Qualitative_8_.add(color(231, 138, 195));
  Set2_Qualitative_8_.add(color(166, 216, 84));
  Set2_Qualitative_8_.add(color(255, 217, 47));
  Set2_Qualitative_8_.add(color(229, 196, 148));
  Set2_Qualitative_8_.add(color(179, 179, 179));
  return Set2_Qualitative_8_;
}


// color Set3_Qualitative_7
public ArrayList get_Set3_Qualitative_7() {
  ArrayList Set3_Qualitative_7_ = new ArrayList();
  Set3_Qualitative_7_.add(color(141, 211, 199));
  Set3_Qualitative_7_.add(color(255, 255, 179));
  Set3_Qualitative_7_.add(color(190, 186, 218));
  Set3_Qualitative_7_.add(color(251, 128, 114));
  Set3_Qualitative_7_.add(color(128, 177, 211));
  Set3_Qualitative_7_.add(color(253, 180, 98));
  Set3_Qualitative_7_.add(color(179, 222, 105));
  return Set3_Qualitative_7_;
}


// color Set3_Qualitative_4
public ArrayList get_Set3_Qualitative_4() {
  ArrayList Set3_Qualitative_4_ = new ArrayList();
  Set3_Qualitative_4_.add(color(141, 211, 199));
  Set3_Qualitative_4_.add(color(255, 255, 179));
  Set3_Qualitative_4_.add(color(190, 186, 218));
  Set3_Qualitative_4_.add(color(251, 128, 114));
  return Set3_Qualitative_4_;
}


// color RdYlBu_Diverging_3
public ArrayList get_RdYlBu_Diverging_3() {
  ArrayList RdYlBu_Diverging_3_ = new ArrayList();
  RdYlBu_Diverging_3_.add(color(252, 141, 89));
  RdYlBu_Diverging_3_.add(color(255, 255, 191));
  RdYlBu_Diverging_3_.add(color(145, 191, 219));
  return RdYlBu_Diverging_3_;
}


// color Set3_Qualitative_5
public ArrayList get_Set3_Qualitative_5() {
  ArrayList Set3_Qualitative_5_ = new ArrayList();
  Set3_Qualitative_5_.add(color(141, 211, 199));
  Set3_Qualitative_5_.add(color(255, 255, 179));
  Set3_Qualitative_5_.add(color(190, 186, 218));
  Set3_Qualitative_5_.add(color(251, 128, 114));
  Set3_Qualitative_5_.add(color(128, 177, 211));
  return Set3_Qualitative_5_;
}


// color Purples_Sequential_3
public ArrayList get_Purples_Sequential_3() {
  ArrayList Purples_Sequential_3_ = new ArrayList();
  Purples_Sequential_3_.add(color(239, 237, 245));
  Purples_Sequential_3_.add(color(188, 189, 220));
  Purples_Sequential_3_.add(color(117, 107, 177));
  return Purples_Sequential_3_;
}


// color Set3_Qualitative_12
public ArrayList get_Set3_Qualitative_12() {
  ArrayList Set3_Qualitative_12_ = new ArrayList();
  Set3_Qualitative_12_.add(color(141, 211, 199));
  Set3_Qualitative_12_.add(color(255, 255, 179));
  Set3_Qualitative_12_.add(color(190, 186, 218));
  Set3_Qualitative_12_.add(color(251, 128, 114));
  Set3_Qualitative_12_.add(color(128, 177, 211));
  Set3_Qualitative_12_.add(color(253, 180, 98));
  Set3_Qualitative_12_.add(color(179, 222, 105));
  Set3_Qualitative_12_.add(color(252, 205, 229));
  Set3_Qualitative_12_.add(color(217, 217, 217));
  Set3_Qualitative_12_.add(color(188, 128, 189));
  Set3_Qualitative_12_.add(color(204, 235, 197));
  Set3_Qualitative_12_.add(color(255, 237, 111));
  return Set3_Qualitative_12_;
}


// color Set1_Qualitative_8
public ArrayList get_Set1_Qualitative_8() {
  ArrayList Set1_Qualitative_8_ = new ArrayList();
  Set1_Qualitative_8_.add(color(228, 26, 28));
  Set1_Qualitative_8_.add(color(55, 126, 184));
  Set1_Qualitative_8_.add(color(77, 175, 74));
  Set1_Qualitative_8_.add(color(152, 78, 163));
  Set1_Qualitative_8_.add(color(255, 127, 0));
  Set1_Qualitative_8_.add(color(255, 255, 51));
  Set1_Qualitative_8_.add(color(166, 86, 40));
  Set1_Qualitative_8_.add(color(247, 129, 191));
  return Set1_Qualitative_8_;
}


// color Set3_Qualitative_10
public ArrayList get_Set3_Qualitative_10() {
  ArrayList Set3_Qualitative_10_ = new ArrayList();
  Set3_Qualitative_10_.add(color(141, 211, 199));
  Set3_Qualitative_10_.add(color(255, 255, 179));
  Set3_Qualitative_10_.add(color(190, 186, 218));
  Set3_Qualitative_10_.add(color(251, 128, 114));
  Set3_Qualitative_10_.add(color(128, 177, 211));
  Set3_Qualitative_10_.add(color(253, 180, 98));
  Set3_Qualitative_10_.add(color(179, 222, 105));
  Set3_Qualitative_10_.add(color(252, 205, 229));
  Set3_Qualitative_10_.add(color(217, 217, 217));
  Set3_Qualitative_10_.add(color(188, 128, 189));
  return Set3_Qualitative_10_;
}


// color Set3_Qualitative_11
public ArrayList get_Set3_Qualitative_11() {
  ArrayList Set3_Qualitative_11_ = new ArrayList();
  Set3_Qualitative_11_.add(color(141, 211, 199));
  Set3_Qualitative_11_.add(color(255, 255, 179));
  Set3_Qualitative_11_.add(color(190, 186, 218));
  Set3_Qualitative_11_.add(color(251, 128, 114));
  Set3_Qualitative_11_.add(color(128, 177, 211));
  Set3_Qualitative_11_.add(color(253, 180, 98));
  Set3_Qualitative_11_.add(color(179, 222, 105));
  Set3_Qualitative_11_.add(color(252, 205, 229));
  Set3_Qualitative_11_.add(color(217, 217, 217));
  Set3_Qualitative_11_.add(color(188, 128, 189));
  Set3_Qualitative_11_.add(color(204, 235, 197));
  return Set3_Qualitative_11_;
}


// color RdBu_Diverging_8
public ArrayList get_RdBu_Diverging_8() {
  ArrayList RdBu_Diverging_8_ = new ArrayList();
  RdBu_Diverging_8_.add(color(178, 24, 43));
  RdBu_Diverging_8_.add(color(214, 96, 77));
  RdBu_Diverging_8_.add(color(244, 165, 130));
  RdBu_Diverging_8_.add(color(253, 219, 199));
  RdBu_Diverging_8_.add(color(209, 229, 240));
  RdBu_Diverging_8_.add(color(146, 197, 222));
  RdBu_Diverging_8_.add(color(67, 147, 195));
  RdBu_Diverging_8_.add(color(33, 102, 172));
  return RdBu_Diverging_8_;
}


// color RdBu_Diverging_9
public ArrayList get_RdBu_Diverging_9() {
  ArrayList RdBu_Diverging_9_ = new ArrayList();
  RdBu_Diverging_9_.add(color(178, 24, 43));
  RdBu_Diverging_9_.add(color(214, 96, 77));
  RdBu_Diverging_9_.add(color(244, 165, 130));
  RdBu_Diverging_9_.add(color(253, 219, 199));
  RdBu_Diverging_9_.add(color(247, 247, 247));
  RdBu_Diverging_9_.add(color(209, 229, 240));
  RdBu_Diverging_9_.add(color(146, 197, 222));
  RdBu_Diverging_9_.add(color(67, 147, 195));
  RdBu_Diverging_9_.add(color(33, 102, 172));
  return RdBu_Diverging_9_;
}


// color Set1_Qualitative_9
public ArrayList get_Set1_Qualitative_9() {
  ArrayList Set1_Qualitative_9_ = new ArrayList();
  Set1_Qualitative_9_.add(color(228, 26, 28));
  Set1_Qualitative_9_.add(color(55, 126, 184));
  Set1_Qualitative_9_.add(color(77, 175, 74));
  Set1_Qualitative_9_.add(color(152, 78, 163));
  Set1_Qualitative_9_.add(color(255, 127, 0));
  Set1_Qualitative_9_.add(color(255, 255, 51));
  Set1_Qualitative_9_.add(color(166, 86, 40));
  Set1_Qualitative_9_.add(color(247, 129, 191));
  Set1_Qualitative_9_.add(color(153, 153, 153));
  return Set1_Qualitative_9_;
}


// color RdYlBu_Diverging_8
public ArrayList get_RdYlBu_Diverging_8() {
  ArrayList RdYlBu_Diverging_8_ = new ArrayList();
  RdYlBu_Diverging_8_.add(color(215, 48, 39));
  RdYlBu_Diverging_8_.add(color(244, 109, 67));
  RdYlBu_Diverging_8_.add(color(253, 174, 97));
  RdYlBu_Diverging_8_.add(color(254, 224, 144));
  RdYlBu_Diverging_8_.add(color(224, 243, 248));
  RdYlBu_Diverging_8_.add(color(171, 217, 233));
  RdYlBu_Diverging_8_.add(color(116, 173, 209));
  RdYlBu_Diverging_8_.add(color(69, 117, 180));
  return RdYlBu_Diverging_8_;
}


// color RdYlBu_Diverging_5
public ArrayList get_RdYlBu_Diverging_5() {
  ArrayList RdYlBu_Diverging_5_ = new ArrayList();
  RdYlBu_Diverging_5_.add(color(215, 25, 28));
  RdYlBu_Diverging_5_.add(color(253, 174, 97));
  RdYlBu_Diverging_5_.add(color(255, 255, 191));
  RdYlBu_Diverging_5_.add(color(171, 217, 233));
  RdYlBu_Diverging_5_.add(color(44, 123, 182));
  return RdYlBu_Diverging_5_;
}


// color RdYlBu_Diverging_10
public ArrayList get_RdYlBu_Diverging_10() {
  ArrayList RdYlBu_Diverging_10_ = new ArrayList();
  RdYlBu_Diverging_10_.add(color(165, 0, 38));
  RdYlBu_Diverging_10_.add(color(215, 48, 39));
  RdYlBu_Diverging_10_.add(color(244, 109, 67));
  RdYlBu_Diverging_10_.add(color(253, 174, 97));
  RdYlBu_Diverging_10_.add(color(254, 224, 144));
  RdYlBu_Diverging_10_.add(color(224, 243, 248));
  RdYlBu_Diverging_10_.add(color(171, 217, 233));
  RdYlBu_Diverging_10_.add(color(116, 173, 209));
  RdYlBu_Diverging_10_.add(color(69, 117, 180));
  RdYlBu_Diverging_10_.add(color(49, 54, 149));
  return RdYlBu_Diverging_10_;
}


// color RdBu_Diverging_3
public ArrayList get_RdBu_Diverging_3() {
  ArrayList RdBu_Diverging_3_ = new ArrayList();
  RdBu_Diverging_3_.add(color(239, 138, 98));
  RdBu_Diverging_3_.add(color(247, 247, 247));
  RdBu_Diverging_3_.add(color(103, 169, 207));
  return RdBu_Diverging_3_;
}


// color RdBu_Diverging_4
public ArrayList get_RdBu_Diverging_4() {
  ArrayList RdBu_Diverging_4_ = new ArrayList();
  RdBu_Diverging_4_.add(color(202, 0, 32));
  RdBu_Diverging_4_.add(color(244, 165, 130));
  RdBu_Diverging_4_.add(color(146, 197, 222));
  RdBu_Diverging_4_.add(color(5, 113, 176));
  return RdBu_Diverging_4_;
}


// color RdBu_Diverging_5
public ArrayList get_RdBu_Diverging_5() {
  ArrayList RdBu_Diverging_5_ = new ArrayList();
  RdBu_Diverging_5_.add(color(202, 0, 32));
  RdBu_Diverging_5_.add(color(244, 165, 130));
  RdBu_Diverging_5_.add(color(247, 247, 247));
  RdBu_Diverging_5_.add(color(146, 197, 222));
  RdBu_Diverging_5_.add(color(5, 113, 176));
  return RdBu_Diverging_5_;
}


// color RdBu_Diverging_6
public ArrayList get_RdBu_Diverging_6() {
  ArrayList RdBu_Diverging_6_ = new ArrayList();
  RdBu_Diverging_6_.add(color(178, 24, 43));
  RdBu_Diverging_6_.add(color(239, 138, 98));
  RdBu_Diverging_6_.add(color(253, 219, 199));
  RdBu_Diverging_6_.add(color(209, 229, 240));
  RdBu_Diverging_6_.add(color(103, 169, 207));
  RdBu_Diverging_6_.add(color(33, 102, 172));
  return RdBu_Diverging_6_;
}


// color RdBu_Diverging_7
public ArrayList get_RdBu_Diverging_7() {
  ArrayList RdBu_Diverging_7_ = new ArrayList();
  RdBu_Diverging_7_.add(color(178, 24, 43));
  RdBu_Diverging_7_.add(color(239, 138, 98));
  RdBu_Diverging_7_.add(color(253, 219, 199));
  RdBu_Diverging_7_.add(color(247, 247, 247));
  RdBu_Diverging_7_.add(color(209, 229, 240));
  RdBu_Diverging_7_.add(color(103, 169, 207));
  RdBu_Diverging_7_.add(color(33, 102, 172));
  return RdBu_Diverging_7_;
}


// color YlGnBu_Sequential_3
public ArrayList get_YlGnBu_Sequential_3() {
  ArrayList YlGnBu_Sequential_3_ = new ArrayList();
  YlGnBu_Sequential_3_.add(color(237, 248, 177));
  YlGnBu_Sequential_3_.add(color(127, 205, 187));
  YlGnBu_Sequential_3_.add(color(44, 127, 184));
  return YlGnBu_Sequential_3_;
}


// color PuOr_Diverging_9
public ArrayList get_PuOr_Diverging_9() {
  ArrayList PuOr_Diverging_9_ = new ArrayList();
  PuOr_Diverging_9_.add(color(179, 88, 6));
  PuOr_Diverging_9_.add(color(224, 130, 20));
  PuOr_Diverging_9_.add(color(253, 184, 99));
  PuOr_Diverging_9_.add(color(254, 224, 182));
  PuOr_Diverging_9_.add(color(247, 247, 247));
  PuOr_Diverging_9_.add(color(216, 218, 235));
  PuOr_Diverging_9_.add(color(178, 171, 210));
  PuOr_Diverging_9_.add(color(128, 115, 172));
  PuOr_Diverging_9_.add(color(84, 39, 136));
  return PuOr_Diverging_9_;
}


// color PuOr_Diverging_8
public ArrayList get_PuOr_Diverging_8() {
  ArrayList PuOr_Diverging_8_ = new ArrayList();
  PuOr_Diverging_8_.add(color(179, 88, 6));
  PuOr_Diverging_8_.add(color(224, 130, 20));
  PuOr_Diverging_8_.add(color(253, 184, 99));
  PuOr_Diverging_8_.add(color(254, 224, 182));
  PuOr_Diverging_8_.add(color(216, 218, 235));
  PuOr_Diverging_8_.add(color(178, 171, 210));
  PuOr_Diverging_8_.add(color(128, 115, 172));
  PuOr_Diverging_8_.add(color(84, 39, 136));
  return PuOr_Diverging_8_;
}


// color Purples_Sequential_8
public ArrayList get_Purples_Sequential_8() {
  ArrayList Purples_Sequential_8_ = new ArrayList();
  Purples_Sequential_8_.add(color(252, 251, 253));
  Purples_Sequential_8_.add(color(239, 237, 245));
  Purples_Sequential_8_.add(color(218, 218, 235));
  Purples_Sequential_8_.add(color(188, 189, 220));
  Purples_Sequential_8_.add(color(158, 154, 200));
  Purples_Sequential_8_.add(color(128, 125, 186));
  Purples_Sequential_8_.add(color(106, 81, 163));
  Purples_Sequential_8_.add(color(74, 20, 134));
  return Purples_Sequential_8_;
}


// color Purples_Sequential_9
public ArrayList get_Purples_Sequential_9() {
  ArrayList Purples_Sequential_9_ = new ArrayList();
  Purples_Sequential_9_.add(color(252, 251, 253));
  Purples_Sequential_9_.add(color(239, 237, 245));
  Purples_Sequential_9_.add(color(218, 218, 235));
  Purples_Sequential_9_.add(color(188, 189, 220));
  Purples_Sequential_9_.add(color(158, 154, 200));
  Purples_Sequential_9_.add(color(128, 125, 186));
  Purples_Sequential_9_.add(color(106, 81, 163));
  Purples_Sequential_9_.add(color(84, 39, 143));
  Purples_Sequential_9_.add(color(63, 0, 125));
  return Purples_Sequential_9_;
}


// color PuOr_Diverging_3
public ArrayList get_PuOr_Diverging_3() {
  ArrayList PuOr_Diverging_3_ = new ArrayList();
  PuOr_Diverging_3_.add(color(241, 163, 64));
  PuOr_Diverging_3_.add(color(247, 247, 247));
  PuOr_Diverging_3_.add(color(153, 142, 195));
  return PuOr_Diverging_3_;
}


// color Purples_Sequential_5
public ArrayList get_Purples_Sequential_5() {
  ArrayList Purples_Sequential_5_ = new ArrayList();
  Purples_Sequential_5_.add(color(242, 240, 247));
  Purples_Sequential_5_.add(color(203, 201, 226));
  Purples_Sequential_5_.add(color(158, 154, 200));
  Purples_Sequential_5_.add(color(117, 107, 177));
  Purples_Sequential_5_.add(color(84, 39, 143));
  return Purples_Sequential_5_;
}


// color Purples_Sequential_6
public ArrayList get_Purples_Sequential_6() {
  ArrayList Purples_Sequential_6_ = new ArrayList();
  Purples_Sequential_6_.add(color(242, 240, 247));
  Purples_Sequential_6_.add(color(218, 218, 235));
  Purples_Sequential_6_.add(color(188, 189, 220));
  Purples_Sequential_6_.add(color(158, 154, 200));
  Purples_Sequential_6_.add(color(117, 107, 177));
  Purples_Sequential_6_.add(color(84, 39, 143));
  return Purples_Sequential_6_;
}


// color Pastel2_Qualitative_8
public ArrayList get_Pastel2_Qualitative_8() {
  ArrayList Pastel2_Qualitative_8_ = new ArrayList();
  Pastel2_Qualitative_8_.add(color(179, 226, 205));
  Pastel2_Qualitative_8_.add(color(253, 205, 172));
  Pastel2_Qualitative_8_.add(color(203, 213, 232));
  Pastel2_Qualitative_8_.add(color(244, 202, 228));
  Pastel2_Qualitative_8_.add(color(230, 245, 201));
  Pastel2_Qualitative_8_.add(color(255, 242, 174));
  Pastel2_Qualitative_8_.add(color(241, 226, 204));
  Pastel2_Qualitative_8_.add(color(204, 204, 204));
  return Pastel2_Qualitative_8_;
}


// color PuOr_Diverging_7
public ArrayList get_PuOr_Diverging_7() {
  ArrayList PuOr_Diverging_7_ = new ArrayList();
  PuOr_Diverging_7_.add(color(179, 88, 6));
  PuOr_Diverging_7_.add(color(241, 163, 64));
  PuOr_Diverging_7_.add(color(254, 224, 182));
  PuOr_Diverging_7_.add(color(247, 247, 247));
  PuOr_Diverging_7_.add(color(216, 218, 235));
  PuOr_Diverging_7_.add(color(153, 142, 195));
  PuOr_Diverging_7_.add(color(84, 39, 136));
  return PuOr_Diverging_7_;
}


// color PuOr_Diverging_6
public ArrayList get_PuOr_Diverging_6() {
  ArrayList PuOr_Diverging_6_ = new ArrayList();
  PuOr_Diverging_6_.add(color(179, 88, 6));
  PuOr_Diverging_6_.add(color(241, 163, 64));
  PuOr_Diverging_6_.add(color(254, 224, 182));
  PuOr_Diverging_6_.add(color(216, 218, 235));
  PuOr_Diverging_6_.add(color(153, 142, 195));
  PuOr_Diverging_6_.add(color(84, 39, 136));
  return PuOr_Diverging_6_;
}


// color PuOr_Diverging_5
public ArrayList get_PuOr_Diverging_5() {
  ArrayList PuOr_Diverging_5_ = new ArrayList();
  PuOr_Diverging_5_.add(color(230, 97, 1));
  PuOr_Diverging_5_.add(color(253, 184, 99));
  PuOr_Diverging_5_.add(color(247, 247, 247));
  PuOr_Diverging_5_.add(color(178, 171, 210));
  PuOr_Diverging_5_.add(color(94, 60, 153));
  return PuOr_Diverging_5_;
}


// color PuOr_Diverging_4
public ArrayList get_PuOr_Diverging_4() {
  ArrayList PuOr_Diverging_4_ = new ArrayList();
  PuOr_Diverging_4_.add(color(230, 97, 1));
  PuOr_Diverging_4_.add(color(253, 184, 99));
  PuOr_Diverging_4_.add(color(178, 171, 210));
  PuOr_Diverging_4_.add(color(94, 60, 153));
  return PuOr_Diverging_4_;
}


// color RdYlGn_Diverging_9
public ArrayList get_RdYlGn_Diverging_9() {
  ArrayList RdYlGn_Diverging_9_ = new ArrayList();
  RdYlGn_Diverging_9_.add(color(215, 48, 39));
  RdYlGn_Diverging_9_.add(color(244, 109, 67));
  RdYlGn_Diverging_9_.add(color(253, 174, 97));
  RdYlGn_Diverging_9_.add(color(254, 224, 139));
  RdYlGn_Diverging_9_.add(color(255, 255, 191));
  RdYlGn_Diverging_9_.add(color(217, 239, 139));
  RdYlGn_Diverging_9_.add(color(166, 217, 106));
  RdYlGn_Diverging_9_.add(color(102, 189, 99));
  RdYlGn_Diverging_9_.add(color(26, 152, 80));
  return RdYlGn_Diverging_9_;
}


// color RdYlGn_Diverging_8
public ArrayList get_RdYlGn_Diverging_8() {
  ArrayList RdYlGn_Diverging_8_ = new ArrayList();
  RdYlGn_Diverging_8_.add(color(215, 48, 39));
  RdYlGn_Diverging_8_.add(color(244, 109, 67));
  RdYlGn_Diverging_8_.add(color(253, 174, 97));
  RdYlGn_Diverging_8_.add(color(254, 224, 139));
  RdYlGn_Diverging_8_.add(color(217, 239, 139));
  RdYlGn_Diverging_8_.add(color(166, 217, 106));
  RdYlGn_Diverging_8_.add(color(102, 189, 99));
  RdYlGn_Diverging_8_.add(color(26, 152, 80));
  return RdYlGn_Diverging_8_;
}


// color Accent_Qualitative_8
public ArrayList get_Accent_Qualitative_8() {
  ArrayList Accent_Qualitative_8_ = new ArrayList();
  Accent_Qualitative_8_.add(color(127, 201, 127));
  Accent_Qualitative_8_.add(color(190, 174, 212));
  Accent_Qualitative_8_.add(color(253, 192, 134));
  Accent_Qualitative_8_.add(color(255, 255, 153));
  Accent_Qualitative_8_.add(color(56, 108, 176));
  Accent_Qualitative_8_.add(color(240, 2, 127));
  Accent_Qualitative_8_.add(color(191, 91, 23));
  Accent_Qualitative_8_.add(color(102, 102, 102));
  return Accent_Qualitative_8_;
}


// color YlGnBu_Sequential_9
public ArrayList get_YlGnBu_Sequential_9() {
  ArrayList YlGnBu_Sequential_9_ = new ArrayList();
  YlGnBu_Sequential_9_.add(color(255, 255, 217));
  YlGnBu_Sequential_9_.add(color(237, 248, 177));
  YlGnBu_Sequential_9_.add(color(199, 233, 180));
  YlGnBu_Sequential_9_.add(color(127, 205, 187));
  YlGnBu_Sequential_9_.add(color(65, 182, 196));
  YlGnBu_Sequential_9_.add(color(29, 145, 192));
  YlGnBu_Sequential_9_.add(color(34, 94, 168));
  YlGnBu_Sequential_9_.add(color(37, 52, 148));
  YlGnBu_Sequential_9_.add(color(8, 29, 88));
  return YlGnBu_Sequential_9_;
}


// color YlGnBu_Sequential_8
public ArrayList get_YlGnBu_Sequential_8() {
  ArrayList YlGnBu_Sequential_8_ = new ArrayList();
  YlGnBu_Sequential_8_.add(color(255, 255, 217));
  YlGnBu_Sequential_8_.add(color(237, 248, 177));
  YlGnBu_Sequential_8_.add(color(199, 233, 180));
  YlGnBu_Sequential_8_.add(color(127, 205, 187));
  YlGnBu_Sequential_8_.add(color(65, 182, 196));
  YlGnBu_Sequential_8_.add(color(29, 145, 192));
  YlGnBu_Sequential_8_.add(color(34, 94, 168));
  YlGnBu_Sequential_8_.add(color(12, 44, 132));
  return YlGnBu_Sequential_8_;
}


// color Accent_Qualitative_3
public ArrayList get_Accent_Qualitative_3() {
  ArrayList Accent_Qualitative_3_ = new ArrayList();
  Accent_Qualitative_3_.add(color(127, 201, 127));
  Accent_Qualitative_3_.add(color(190, 174, 212));
  Accent_Qualitative_3_.add(color(253, 192, 134));
  return Accent_Qualitative_3_;
}


// color YlGn_Sequential_7
public ArrayList get_YlGn_Sequential_7() {
  ArrayList YlGn_Sequential_7_ = new ArrayList();
  YlGn_Sequential_7_.add(color(255, 255, 204));
  YlGn_Sequential_7_.add(color(217, 240, 163));
  YlGn_Sequential_7_.add(color(173, 221, 142));
  YlGn_Sequential_7_.add(color(120, 198, 121));
  YlGn_Sequential_7_.add(color(65, 171, 93));
  YlGn_Sequential_7_.add(color(35, 132, 67));
  YlGn_Sequential_7_.add(color(0, 90, 50));
  return YlGn_Sequential_7_;
}


// color RdYlGn_Diverging_3
public ArrayList get_RdYlGn_Diverging_3() {
  ArrayList RdYlGn_Diverging_3_ = new ArrayList();
  RdYlGn_Diverging_3_.add(color(252, 141, 89));
  RdYlGn_Diverging_3_.add(color(255, 255, 191));
  RdYlGn_Diverging_3_.add(color(145, 207, 96));
  return RdYlGn_Diverging_3_;
}


// color YlGnBu_Sequential_6
public ArrayList get_YlGnBu_Sequential_6() {
  ArrayList YlGnBu_Sequential_6_ = new ArrayList();
  YlGnBu_Sequential_6_.add(color(255, 255, 204));
  YlGnBu_Sequential_6_.add(color(199, 233, 180));
  YlGnBu_Sequential_6_.add(color(127, 205, 187));
  YlGnBu_Sequential_6_.add(color(65, 182, 196));
  YlGnBu_Sequential_6_.add(color(44, 127, 184));
  YlGnBu_Sequential_6_.add(color(37, 52, 148));
  return YlGnBu_Sequential_6_;
}


// color Accent_Qualitative_7
public ArrayList get_Accent_Qualitative_7() {
  ArrayList Accent_Qualitative_7_ = new ArrayList();
  Accent_Qualitative_7_.add(color(127, 201, 127));
  Accent_Qualitative_7_.add(color(190, 174, 212));
  Accent_Qualitative_7_.add(color(253, 192, 134));
  Accent_Qualitative_7_.add(color(255, 255, 153));
  Accent_Qualitative_7_.add(color(56, 108, 176));
  Accent_Qualitative_7_.add(color(240, 2, 127));
  Accent_Qualitative_7_.add(color(191, 91, 23));
  return Accent_Qualitative_7_;
}


// color Accent_Qualitative_6
public ArrayList get_Accent_Qualitative_6() {
  ArrayList Accent_Qualitative_6_ = new ArrayList();
  Accent_Qualitative_6_.add(color(127, 201, 127));
  Accent_Qualitative_6_.add(color(190, 174, 212));
  Accent_Qualitative_6_.add(color(253, 192, 134));
  Accent_Qualitative_6_.add(color(255, 255, 153));
  Accent_Qualitative_6_.add(color(56, 108, 176));
  Accent_Qualitative_6_.add(color(240, 2, 127));
  return Accent_Qualitative_6_;
}


// color Accent_Qualitative_5
public ArrayList get_Accent_Qualitative_5() {
  ArrayList Accent_Qualitative_5_ = new ArrayList();
  Accent_Qualitative_5_.add(color(127, 201, 127));
  Accent_Qualitative_5_.add(color(190, 174, 212));
  Accent_Qualitative_5_.add(color(253, 192, 134));
  Accent_Qualitative_5_.add(color(255, 255, 153));
  Accent_Qualitative_5_.add(color(56, 108, 176));
  return Accent_Qualitative_5_;
}


// color Accent_Qualitative_4
public ArrayList get_Accent_Qualitative_4() {
  ArrayList Accent_Qualitative_4_ = new ArrayList();
  Accent_Qualitative_4_.add(color(127, 201, 127));
  Accent_Qualitative_4_.add(color(190, 174, 212));
  Accent_Qualitative_4_.add(color(253, 192, 134));
  Accent_Qualitative_4_.add(color(255, 255, 153));
  return Accent_Qualitative_4_;
}


// color Reds_Sequential_6
public ArrayList get_Reds_Sequential_6() {
  ArrayList Reds_Sequential_6_ = new ArrayList();
  Reds_Sequential_6_.add(color(254, 229, 217));
  Reds_Sequential_6_.add(color(252, 187, 161));
  Reds_Sequential_6_.add(color(252, 146, 114));
  Reds_Sequential_6_.add(color(251, 106, 74));
  Reds_Sequential_6_.add(color(222, 45, 38));
  Reds_Sequential_6_.add(color(165, 15, 21));
  return Reds_Sequential_6_;
}


// color Reds_Sequential_7
public ArrayList get_Reds_Sequential_7() {
  ArrayList Reds_Sequential_7_ = new ArrayList();
  Reds_Sequential_7_.add(color(254, 229, 217));
  Reds_Sequential_7_.add(color(252, 187, 161));
  Reds_Sequential_7_.add(color(252, 146, 114));
  Reds_Sequential_7_.add(color(251, 106, 74));
  Reds_Sequential_7_.add(color(239, 59, 44));
  Reds_Sequential_7_.add(color(203, 24, 29));
  Reds_Sequential_7_.add(color(153, 0, 13));
  return Reds_Sequential_7_;
}


// color Reds_Sequential_4
public ArrayList get_Reds_Sequential_4() {
  ArrayList Reds_Sequential_4_ = new ArrayList();
  Reds_Sequential_4_.add(color(254, 229, 217));
  Reds_Sequential_4_.add(color(252, 174, 145));
  Reds_Sequential_4_.add(color(251, 106, 74));
  Reds_Sequential_4_.add(color(203, 24, 29));
  return Reds_Sequential_4_;
}


// color Reds_Sequential_5
public ArrayList get_Reds_Sequential_5() {
  ArrayList Reds_Sequential_5_ = new ArrayList();
  Reds_Sequential_5_.add(color(254, 229, 217));
  Reds_Sequential_5_.add(color(252, 174, 145));
  Reds_Sequential_5_.add(color(251, 106, 74));
  Reds_Sequential_5_.add(color(222, 45, 38));
  Reds_Sequential_5_.add(color(165, 15, 21));
  return Reds_Sequential_5_;
}


// color Reds_Sequential_3
public ArrayList get_Reds_Sequential_3() {
  ArrayList Reds_Sequential_3_ = new ArrayList();
  Reds_Sequential_3_.add(color(254, 224, 210));
  Reds_Sequential_3_.add(color(252, 146, 114));
  Reds_Sequential_3_.add(color(222, 45, 38));
  return Reds_Sequential_3_;
}


// color YlGnBu_Sequential_4
public ArrayList get_YlGnBu_Sequential_4() {
  ArrayList YlGnBu_Sequential_4_ = new ArrayList();
  YlGnBu_Sequential_4_.add(color(255, 255, 204));
  YlGnBu_Sequential_4_.add(color(161, 218, 180));
  YlGnBu_Sequential_4_.add(color(65, 182, 196));
  YlGnBu_Sequential_4_.add(color(34, 94, 168));
  return YlGnBu_Sequential_4_;
}


// color YlGnBu_Sequential_7
public ArrayList get_YlGnBu_Sequential_7() {
  ArrayList YlGnBu_Sequential_7_ = new ArrayList();
  YlGnBu_Sequential_7_.add(color(255, 255, 204));
  YlGnBu_Sequential_7_.add(color(199, 233, 180));
  YlGnBu_Sequential_7_.add(color(127, 205, 187));
  YlGnBu_Sequential_7_.add(color(65, 182, 196));
  YlGnBu_Sequential_7_.add(color(29, 145, 192));
  YlGnBu_Sequential_7_.add(color(34, 94, 168));
  YlGnBu_Sequential_7_.add(color(12, 44, 132));
  return YlGnBu_Sequential_7_;
}


// color Reds_Sequential_8
public ArrayList get_Reds_Sequential_8() {
  ArrayList Reds_Sequential_8_ = new ArrayList();
  Reds_Sequential_8_.add(color(255, 245, 240));
  Reds_Sequential_8_.add(color(254, 224, 210));
  Reds_Sequential_8_.add(color(252, 187, 161));
  Reds_Sequential_8_.add(color(252, 146, 114));
  Reds_Sequential_8_.add(color(251, 106, 74));
  Reds_Sequential_8_.add(color(239, 59, 44));
  Reds_Sequential_8_.add(color(203, 24, 29));
  Reds_Sequential_8_.add(color(153, 0, 13));
  return Reds_Sequential_8_;
}


// color Reds_Sequential_9
public ArrayList get_Reds_Sequential_9() {
  ArrayList Reds_Sequential_9_ = new ArrayList();
  Reds_Sequential_9_.add(color(255, 245, 240));
  Reds_Sequential_9_.add(color(254, 224, 210));
  Reds_Sequential_9_.add(color(252, 187, 161));
  Reds_Sequential_9_.add(color(252, 146, 114));
  Reds_Sequential_9_.add(color(251, 106, 74));
  Reds_Sequential_9_.add(color(239, 59, 44));
  Reds_Sequential_9_.add(color(203, 24, 29));
  Reds_Sequential_9_.add(color(165, 15, 21));
  Reds_Sequential_9_.add(color(103, 0, 13));
  return Reds_Sequential_9_;
}


// color PuBuGn_Sequential_7
public ArrayList get_PuBuGn_Sequential_7() {
  ArrayList PuBuGn_Sequential_7_ = new ArrayList();
  PuBuGn_Sequential_7_.add(color(246, 239, 247));
  PuBuGn_Sequential_7_.add(color(208, 209, 230));
  PuBuGn_Sequential_7_.add(color(166, 189, 219));
  PuBuGn_Sequential_7_.add(color(103, 169, 207));
  PuBuGn_Sequential_7_.add(color(54, 144, 192));
  PuBuGn_Sequential_7_.add(color(2, 129, 138));
  PuBuGn_Sequential_7_.add(color(1, 100, 80));
  return PuBuGn_Sequential_7_;
}


// color PuBu_Sequential_8
public ArrayList get_PuBu_Sequential_8() {
  ArrayList PuBu_Sequential_8_ = new ArrayList();
  PuBu_Sequential_8_.add(color(255, 247, 251));
  PuBu_Sequential_8_.add(color(236, 231, 242));
  PuBu_Sequential_8_.add(color(208, 209, 230));
  PuBu_Sequential_8_.add(color(166, 189, 219));
  PuBu_Sequential_8_.add(color(116, 169, 207));
  PuBu_Sequential_8_.add(color(54, 144, 192));
  PuBu_Sequential_8_.add(color(5, 112, 176));
  PuBu_Sequential_8_.add(color(3, 78, 123));
  return PuBu_Sequential_8_;
}


// color PuBuGn_Sequential_5
public ArrayList get_PuBuGn_Sequential_5() {
  ArrayList PuBuGn_Sequential_5_ = new ArrayList();
  PuBuGn_Sequential_5_.add(color(246, 239, 247));
  PuBuGn_Sequential_5_.add(color(189, 201, 225));
  PuBuGn_Sequential_5_.add(color(103, 169, 207));
  PuBuGn_Sequential_5_.add(color(28, 144, 153));
  PuBuGn_Sequential_5_.add(color(1, 108, 89));
  return PuBuGn_Sequential_5_;
}


// color PuBuGn_Sequential_4
public ArrayList get_PuBuGn_Sequential_4() {
  ArrayList PuBuGn_Sequential_4_ = new ArrayList();
  PuBuGn_Sequential_4_.add(color(246, 239, 247));
  PuBuGn_Sequential_4_.add(color(189, 201, 225));
  PuBuGn_Sequential_4_.add(color(103, 169, 207));
  PuBuGn_Sequential_4_.add(color(2, 129, 138));
  return PuBuGn_Sequential_4_;
}


// color PuBuGn_Sequential_3
public ArrayList get_PuBuGn_Sequential_3() {
  ArrayList PuBuGn_Sequential_3_ = new ArrayList();
  PuBuGn_Sequential_3_.add(color(236, 226, 240));
  PuBuGn_Sequential_3_.add(color(166, 189, 219));
  PuBuGn_Sequential_3_.add(color(28, 144, 153));
  return PuBuGn_Sequential_3_;
}


// color PuBu_Sequential_9
public ArrayList get_PuBu_Sequential_9() {
  ArrayList PuBu_Sequential_9_ = new ArrayList();
  PuBu_Sequential_9_.add(color(255, 247, 251));
  PuBu_Sequential_9_.add(color(236, 231, 242));
  PuBu_Sequential_9_.add(color(208, 209, 230));
  PuBu_Sequential_9_.add(color(166, 189, 219));
  PuBu_Sequential_9_.add(color(116, 169, 207));
  PuBu_Sequential_9_.add(color(54, 144, 192));
  PuBu_Sequential_9_.add(color(5, 112, 176));
  PuBu_Sequential_9_.add(color(4, 90, 141));
  PuBu_Sequential_9_.add(color(2, 56, 88));
  return PuBu_Sequential_9_;
}


// color YlOrRd_Sequential_6
public ArrayList get_YlOrRd_Sequential_6() {
  ArrayList YlOrRd_Sequential_6_ = new ArrayList();
  YlOrRd_Sequential_6_.add(color(255, 255, 178));
  YlOrRd_Sequential_6_.add(color(254, 217, 118));
  YlOrRd_Sequential_6_.add(color(254, 178, 76));
  YlOrRd_Sequential_6_.add(color(253, 141, 60));
  YlOrRd_Sequential_6_.add(color(240, 59, 32));
  YlOrRd_Sequential_6_.add(color(189, 0, 38));
  return YlOrRd_Sequential_6_;
}


// color RdYlGn_Diverging_5
public ArrayList get_RdYlGn_Diverging_5() {
  ArrayList RdYlGn_Diverging_5_ = new ArrayList();
  RdYlGn_Diverging_5_.add(color(215, 25, 28));
  RdYlGn_Diverging_5_.add(color(253, 174, 97));
  RdYlGn_Diverging_5_.add(color(255, 255, 191));
  RdYlGn_Diverging_5_.add(color(166, 217, 106));
  RdYlGn_Diverging_5_.add(color(26, 150, 65));
  return RdYlGn_Diverging_5_;
}


// color PuBuGn_Sequential_9
public ArrayList get_PuBuGn_Sequential_9() {
  ArrayList PuBuGn_Sequential_9_ = new ArrayList();
  PuBuGn_Sequential_9_.add(color(255, 247, 251));
  PuBuGn_Sequential_9_.add(color(236, 226, 240));
  PuBuGn_Sequential_9_.add(color(208, 209, 230));
  PuBuGn_Sequential_9_.add(color(166, 189, 219));
  PuBuGn_Sequential_9_.add(color(103, 169, 207));
  PuBuGn_Sequential_9_.add(color(54, 144, 192));
  PuBuGn_Sequential_9_.add(color(2, 129, 138));
  PuBuGn_Sequential_9_.add(color(1, 108, 89));
  PuBuGn_Sequential_9_.add(color(1, 70, 54));
  return PuBuGn_Sequential_9_;
}


// color PuBuGn_Sequential_8
public ArrayList get_PuBuGn_Sequential_8() {
  ArrayList PuBuGn_Sequential_8_ = new ArrayList();
  PuBuGn_Sequential_8_.add(color(255, 247, 251));
  PuBuGn_Sequential_8_.add(color(236, 226, 240));
  PuBuGn_Sequential_8_.add(color(208, 209, 230));
  PuBuGn_Sequential_8_.add(color(166, 189, 219));
  PuBuGn_Sequential_8_.add(color(103, 169, 207));
  PuBuGn_Sequential_8_.add(color(54, 144, 192));
  PuBuGn_Sequential_8_.add(color(2, 129, 138));
  PuBuGn_Sequential_8_.add(color(1, 100, 80));
  return PuBuGn_Sequential_8_;
}


// color RdPu_Sequential_3
public ArrayList get_RdPu_Sequential_3() {
  ArrayList RdPu_Sequential_3_ = new ArrayList();
  RdPu_Sequential_3_.add(color(253, 224, 221));
  RdPu_Sequential_3_.add(color(250, 159, 181));
  RdPu_Sequential_3_.add(color(197, 27, 138));
  return RdPu_Sequential_3_;
}


// color Greys_Sequential_3
public ArrayList get_Greys_Sequential_3() {
  ArrayList Greys_Sequential_3_ = new ArrayList();
  Greys_Sequential_3_.add(color(240, 240, 240));
  Greys_Sequential_3_.add(color(189, 189, 189));
  Greys_Sequential_3_.add(color(99, 99, 99));
  return Greys_Sequential_3_;
}


// color Greys_Sequential_5
public ArrayList get_Greys_Sequential_5() {
  ArrayList Greys_Sequential_5_ = new ArrayList();
  Greys_Sequential_5_.add(color(247, 247, 247));
  Greys_Sequential_5_.add(color(204, 204, 204));
  Greys_Sequential_5_.add(color(150, 150, 150));
  Greys_Sequential_5_.add(color(99, 99, 99));
  Greys_Sequential_5_.add(color(37, 37, 37));
  return Greys_Sequential_5_;
}


// color Greys_Sequential_4
public ArrayList get_Greys_Sequential_4() {
  ArrayList Greys_Sequential_4_ = new ArrayList();
  Greys_Sequential_4_.add(color(247, 247, 247));
  Greys_Sequential_4_.add(color(204, 204, 204));
  Greys_Sequential_4_.add(color(150, 150, 150));
  Greys_Sequential_4_.add(color(82, 82, 82));
  return Greys_Sequential_4_;
}


// color Greys_Sequential_7
public ArrayList get_Greys_Sequential_7() {
  ArrayList Greys_Sequential_7_ = new ArrayList();
  Greys_Sequential_7_.add(color(247, 247, 247));
  Greys_Sequential_7_.add(color(217, 217, 217));
  Greys_Sequential_7_.add(color(189, 189, 189));
  Greys_Sequential_7_.add(color(150, 150, 150));
  Greys_Sequential_7_.add(color(115, 115, 115));
  Greys_Sequential_7_.add(color(82, 82, 82));
  Greys_Sequential_7_.add(color(37, 37, 37));
  return Greys_Sequential_7_;
}


// color Greys_Sequential_6
public ArrayList get_Greys_Sequential_6() {
  ArrayList Greys_Sequential_6_ = new ArrayList();
  Greys_Sequential_6_.add(color(247, 247, 247));
  Greys_Sequential_6_.add(color(217, 217, 217));
  Greys_Sequential_6_.add(color(189, 189, 189));
  Greys_Sequential_6_.add(color(150, 150, 150));
  Greys_Sequential_6_.add(color(99, 99, 99));
  Greys_Sequential_6_.add(color(37, 37, 37));
  return Greys_Sequential_6_;
}


// color Greys_Sequential_9
public ArrayList get_Greys_Sequential_9() {
  ArrayList Greys_Sequential_9_ = new ArrayList();
  Greys_Sequential_9_.add(color(255, 255, 255));
  Greys_Sequential_9_.add(color(240, 240, 240));
  Greys_Sequential_9_.add(color(217, 217, 217));
  Greys_Sequential_9_.add(color(189, 189, 189));
  Greys_Sequential_9_.add(color(150, 150, 150));
  Greys_Sequential_9_.add(color(115, 115, 115));
  Greys_Sequential_9_.add(color(82, 82, 82));
  Greys_Sequential_9_.add(color(37, 37, 37));
  Greys_Sequential_9_.add(color(0, 0, 0));
  return Greys_Sequential_9_;
}


// color Greys_Sequential_8
public ArrayList get_Greys_Sequential_8() {
  ArrayList Greys_Sequential_8_ = new ArrayList();
  Greys_Sequential_8_.add(color(255, 255, 255));
  Greys_Sequential_8_.add(color(240, 240, 240));
  Greys_Sequential_8_.add(color(217, 217, 217));
  Greys_Sequential_8_.add(color(189, 189, 189));
  Greys_Sequential_8_.add(color(150, 150, 150));
  Greys_Sequential_8_.add(color(115, 115, 115));
  Greys_Sequential_8_.add(color(82, 82, 82));
  Greys_Sequential_8_.add(color(37, 37, 37));
  return Greys_Sequential_8_;
}


// color RdPu_Sequential_9
public ArrayList get_RdPu_Sequential_9() {
  ArrayList RdPu_Sequential_9_ = new ArrayList();
  RdPu_Sequential_9_.add(color(255, 247, 243));
  RdPu_Sequential_9_.add(color(253, 224, 221));
  RdPu_Sequential_9_.add(color(252, 197, 192));
  RdPu_Sequential_9_.add(color(250, 159, 181));
  RdPu_Sequential_9_.add(color(247, 104, 161));
  RdPu_Sequential_9_.add(color(221, 52, 151));
  RdPu_Sequential_9_.add(color(174, 1, 126));
  RdPu_Sequential_9_.add(color(122, 1, 119));
  RdPu_Sequential_9_.add(color(73, 0, 106));
  return RdPu_Sequential_9_;
}


// color RdPu_Sequential_8
public ArrayList get_RdPu_Sequential_8() {
  ArrayList RdPu_Sequential_8_ = new ArrayList();
  RdPu_Sequential_8_.add(color(255, 247, 243));
  RdPu_Sequential_8_.add(color(253, 224, 221));
  RdPu_Sequential_8_.add(color(252, 197, 192));
  RdPu_Sequential_8_.add(color(250, 159, 181));
  RdPu_Sequential_8_.add(color(247, 104, 161));
  RdPu_Sequential_8_.add(color(221, 52, 151));
  RdPu_Sequential_8_.add(color(174, 1, 126));
  RdPu_Sequential_8_.add(color(122, 1, 119));
  return RdPu_Sequential_8_;
}


// color OrRd_Sequential_9
public ArrayList get_OrRd_Sequential_9() {
  ArrayList OrRd_Sequential_9_ = new ArrayList();
  OrRd_Sequential_9_.add(color(255, 247, 236));
  OrRd_Sequential_9_.add(color(254, 232, 200));
  OrRd_Sequential_9_.add(color(253, 212, 158));
  OrRd_Sequential_9_.add(color(253, 187, 132));
  OrRd_Sequential_9_.add(color(252, 141, 89));
  OrRd_Sequential_9_.add(color(239, 101, 72));
  OrRd_Sequential_9_.add(color(215, 48, 31));
  OrRd_Sequential_9_.add(color(179, 0, 0));
  OrRd_Sequential_9_.add(color(127, 0, 0));
  return OrRd_Sequential_9_;
}


// color OrRd_Sequential_8
public ArrayList get_OrRd_Sequential_8() {
  ArrayList OrRd_Sequential_8_ = new ArrayList();
  OrRd_Sequential_8_.add(color(255, 247, 236));
  OrRd_Sequential_8_.add(color(254, 232, 200));
  OrRd_Sequential_8_.add(color(253, 212, 158));
  OrRd_Sequential_8_.add(color(253, 187, 132));
  OrRd_Sequential_8_.add(color(252, 141, 89));
  OrRd_Sequential_8_.add(color(239, 101, 72));
  OrRd_Sequential_8_.add(color(215, 48, 31));
  OrRd_Sequential_8_.add(color(153, 0, 0));
  return OrRd_Sequential_8_;
}


// color RdYlBu_Diverging_6
public ArrayList get_RdYlBu_Diverging_6() {
  ArrayList RdYlBu_Diverging_6_ = new ArrayList();
  RdYlBu_Diverging_6_.add(color(215, 48, 39));
  RdYlBu_Diverging_6_.add(color(252, 141, 89));
  RdYlBu_Diverging_6_.add(color(254, 224, 144));
  RdYlBu_Diverging_6_.add(color(224, 243, 248));
  RdYlBu_Diverging_6_.add(color(145, 191, 219));
  RdYlBu_Diverging_6_.add(color(69, 117, 180));
  return RdYlBu_Diverging_6_;
}


// color Set3_Qualitative_3
public ArrayList get_Set3_Qualitative_3() {
  ArrayList Set3_Qualitative_3_ = new ArrayList();
  Set3_Qualitative_3_.add(color(141, 211, 199));
  Set3_Qualitative_3_.add(color(255, 255, 179));
  Set3_Qualitative_3_.add(color(190, 186, 218));
  return Set3_Qualitative_3_;
}


// color OrRd_Sequential_3
public ArrayList get_OrRd_Sequential_3() {
  ArrayList OrRd_Sequential_3_ = new ArrayList();
  OrRd_Sequential_3_.add(color(254, 232, 200));
  OrRd_Sequential_3_.add(color(253, 187, 132));
  OrRd_Sequential_3_.add(color(227, 74, 51));
  return OrRd_Sequential_3_;
}


// color OrRd_Sequential_5
public ArrayList get_OrRd_Sequential_5() {
  ArrayList OrRd_Sequential_5_ = new ArrayList();
  OrRd_Sequential_5_.add(color(254, 240, 217));
  OrRd_Sequential_5_.add(color(253, 204, 138));
  OrRd_Sequential_5_.add(color(252, 141, 89));
  OrRd_Sequential_5_.add(color(227, 74, 51));
  OrRd_Sequential_5_.add(color(179, 0, 0));
  return OrRd_Sequential_5_;
}


// color OrRd_Sequential_4
public ArrayList get_OrRd_Sequential_4() {
  ArrayList OrRd_Sequential_4_ = new ArrayList();
  OrRd_Sequential_4_.add(color(254, 240, 217));
  OrRd_Sequential_4_.add(color(253, 204, 138));
  OrRd_Sequential_4_.add(color(252, 141, 89));
  OrRd_Sequential_4_.add(color(215, 48, 31));
  return OrRd_Sequential_4_;
}


// color OrRd_Sequential_7
public ArrayList get_OrRd_Sequential_7() {
  ArrayList OrRd_Sequential_7_ = new ArrayList();
  OrRd_Sequential_7_.add(color(254, 240, 217));
  OrRd_Sequential_7_.add(color(253, 212, 158));
  OrRd_Sequential_7_.add(color(253, 187, 132));
  OrRd_Sequential_7_.add(color(252, 141, 89));
  OrRd_Sequential_7_.add(color(239, 101, 72));
  OrRd_Sequential_7_.add(color(215, 48, 31));
  OrRd_Sequential_7_.add(color(153, 0, 0));
  return OrRd_Sequential_7_;
}


// color OrRd_Sequential_6
public ArrayList get_OrRd_Sequential_6() {
  ArrayList OrRd_Sequential_6_ = new ArrayList();
  OrRd_Sequential_6_.add(color(254, 240, 217));
  OrRd_Sequential_6_.add(color(253, 212, 158));
  OrRd_Sequential_6_.add(color(253, 187, 132));
  OrRd_Sequential_6_.add(color(252, 141, 89));
  OrRd_Sequential_6_.add(color(227, 74, 51));
  OrRd_Sequential_6_.add(color(179, 0, 0));
  return OrRd_Sequential_6_;
}


// color YlGn_Sequential_3
public ArrayList get_YlGn_Sequential_3() {
  ArrayList YlGn_Sequential_3_ = new ArrayList();
  YlGn_Sequential_3_.add(color(247, 252, 185));
  YlGn_Sequential_3_.add(color(173, 221, 142));
  YlGn_Sequential_3_.add(color(49, 163, 84));
  return YlGn_Sequential_3_;
}


// color __0
public ArrayList get___0() {
  ArrayList __0_ = new ArrayList();
  return __0_;
}


// color Blues_Sequential_8
public ArrayList get_Blues_Sequential_8() {
  ArrayList Blues_Sequential_8_ = new ArrayList();
  Blues_Sequential_8_.add(color(247, 251, 255));
  Blues_Sequential_8_.add(color(222, 235, 247));
  Blues_Sequential_8_.add(color(198, 219, 239));
  Blues_Sequential_8_.add(color(158, 202, 225));
  Blues_Sequential_8_.add(color(107, 174, 214));
  Blues_Sequential_8_.add(color(66, 146, 198));
  Blues_Sequential_8_.add(color(33, 113, 181));
  Blues_Sequential_8_.add(color(8, 69, 148));
  return Blues_Sequential_8_;
}


// color Blues_Sequential_9
public ArrayList get_Blues_Sequential_9() {
  ArrayList Blues_Sequential_9_ = new ArrayList();
  Blues_Sequential_9_.add(color(247, 251, 255));
  Blues_Sequential_9_.add(color(222, 235, 247));
  Blues_Sequential_9_.add(color(198, 219, 239));
  Blues_Sequential_9_.add(color(158, 202, 225));
  Blues_Sequential_9_.add(color(107, 174, 214));
  Blues_Sequential_9_.add(color(66, 146, 198));
  Blues_Sequential_9_.add(color(33, 113, 181));
  Blues_Sequential_9_.add(color(8, 81, 156));
  Blues_Sequential_9_.add(color(8, 48, 107));
  return Blues_Sequential_9_;
}


// color PuBu_Sequential_3
public ArrayList get_PuBu_Sequential_3() {
  ArrayList PuBu_Sequential_3_ = new ArrayList();
  PuBu_Sequential_3_.add(color(236, 231, 242));
  PuBu_Sequential_3_.add(color(166, 189, 219));
  PuBu_Sequential_3_.add(color(43, 140, 190));
  return PuBu_Sequential_3_;
}


// color PuBu_Sequential_4
public ArrayList get_PuBu_Sequential_4() {
  ArrayList PuBu_Sequential_4_ = new ArrayList();
  PuBu_Sequential_4_.add(color(241, 238, 246));
  PuBu_Sequential_4_.add(color(189, 201, 225));
  PuBu_Sequential_4_.add(color(116, 169, 207));
  PuBu_Sequential_4_.add(color(5, 112, 176));
  return PuBu_Sequential_4_;
}


// color Blues_Sequential_3
public ArrayList get_Blues_Sequential_3() {
  ArrayList Blues_Sequential_3_ = new ArrayList();
  Blues_Sequential_3_.add(color(222, 235, 247));
  Blues_Sequential_3_.add(color(158, 202, 225));
  Blues_Sequential_3_.add(color(49, 130, 189));
  return Blues_Sequential_3_;
}


// color Blues_Sequential_4
public ArrayList get_Blues_Sequential_4() {
  ArrayList Blues_Sequential_4_ = new ArrayList();
  Blues_Sequential_4_.add(color(239, 243, 255));
  Blues_Sequential_4_.add(color(189, 215, 231));
  Blues_Sequential_4_.add(color(107, 174, 214));
  Blues_Sequential_4_.add(color(33, 113, 181));
  return Blues_Sequential_4_;
}


// color Blues_Sequential_5
public ArrayList get_Blues_Sequential_5() {
  ArrayList Blues_Sequential_5_ = new ArrayList();
  Blues_Sequential_5_.add(color(239, 243, 255));
  Blues_Sequential_5_.add(color(189, 215, 231));
  Blues_Sequential_5_.add(color(107, 174, 214));
  Blues_Sequential_5_.add(color(49, 130, 189));
  Blues_Sequential_5_.add(color(8, 81, 156));
  return Blues_Sequential_5_;
}


// color Blues_Sequential_6
public ArrayList get_Blues_Sequential_6() {
  ArrayList Blues_Sequential_6_ = new ArrayList();
  Blues_Sequential_6_.add(color(239, 243, 255));
  Blues_Sequential_6_.add(color(198, 219, 239));
  Blues_Sequential_6_.add(color(158, 202, 225));
  Blues_Sequential_6_.add(color(107, 174, 214));
  Blues_Sequential_6_.add(color(49, 130, 189));
  Blues_Sequential_6_.add(color(8, 81, 156));
  return Blues_Sequential_6_;
}


// color Blues_Sequential_7
public ArrayList get_Blues_Sequential_7() {
  ArrayList Blues_Sequential_7_ = new ArrayList();
  Blues_Sequential_7_.add(color(239, 243, 255));
  Blues_Sequential_7_.add(color(198, 219, 239));
  Blues_Sequential_7_.add(color(158, 202, 225));
  Blues_Sequential_7_.add(color(107, 174, 214));
  Blues_Sequential_7_.add(color(66, 146, 198));
  Blues_Sequential_7_.add(color(33, 113, 181));
  Blues_Sequential_7_.add(color(8, 69, 148));
  return Blues_Sequential_7_;
}


// color Set2_Qualitative_6
public ArrayList get_Set2_Qualitative_6() {
  ArrayList Set2_Qualitative_6_ = new ArrayList();
  Set2_Qualitative_6_.add(color(102, 194, 165));
  Set2_Qualitative_6_.add(color(252, 141, 98));
  Set2_Qualitative_6_.add(color(141, 160, 203));
  Set2_Qualitative_6_.add(color(231, 138, 195));
  Set2_Qualitative_6_.add(color(166, 216, 84));
  Set2_Qualitative_6_.add(color(255, 217, 47));
  return Set2_Qualitative_6_;
}


// color PuBu_Sequential_6
public ArrayList get_PuBu_Sequential_6() {
  ArrayList PuBu_Sequential_6_ = new ArrayList();
  PuBu_Sequential_6_.add(color(241, 238, 246));
  PuBu_Sequential_6_.add(color(208, 209, 230));
  PuBu_Sequential_6_.add(color(166, 189, 219));
  PuBu_Sequential_6_.add(color(116, 169, 207));
  PuBu_Sequential_6_.add(color(43, 140, 190));
  PuBu_Sequential_6_.add(color(4, 90, 141));
  return PuBu_Sequential_6_;
}


// color BrBG_Diverging_10
public ArrayList get_BrBG_Diverging_10() {
  ArrayList BrBG_Diverging_10_ = new ArrayList();
  BrBG_Diverging_10_.add(color(84, 48, 5));
  BrBG_Diverging_10_.add(color(140, 81, 10));
  BrBG_Diverging_10_.add(color(191, 129, 45));
  BrBG_Diverging_10_.add(color(223, 194, 125));
  BrBG_Diverging_10_.add(color(246, 232, 195));
  BrBG_Diverging_10_.add(color(199, 234, 229));
  BrBG_Diverging_10_.add(color(128, 205, 193));
  BrBG_Diverging_10_.add(color(53, 151, 143));
  BrBG_Diverging_10_.add(color(1, 102, 94));
  BrBG_Diverging_10_.add(color(0, 60, 48));
  return BrBG_Diverging_10_;
}


// color BrBG_Diverging_11
public ArrayList get_BrBG_Diverging_11() {
  ArrayList BrBG_Diverging_11_ = new ArrayList();
  BrBG_Diverging_11_.add(color(84, 48, 5));
  BrBG_Diverging_11_.add(color(140, 81, 10));
  BrBG_Diverging_11_.add(color(191, 129, 45));
  BrBG_Diverging_11_.add(color(223, 194, 125));
  BrBG_Diverging_11_.add(color(246, 232, 195));
  BrBG_Diverging_11_.add(color(245, 245, 245));
  BrBG_Diverging_11_.add(color(199, 234, 229));
  BrBG_Diverging_11_.add(color(128, 205, 193));
  BrBG_Diverging_11_.add(color(53, 151, 143));
  BrBG_Diverging_11_.add(color(1, 102, 94));
  BrBG_Diverging_11_.add(color(0, 60, 48));
  return BrBG_Diverging_11_;
}


// color PuBu_Sequential_7
public ArrayList get_PuBu_Sequential_7() {
  ArrayList PuBu_Sequential_7_ = new ArrayList();
  PuBu_Sequential_7_.add(color(241, 238, 246));
  PuBu_Sequential_7_.add(color(208, 209, 230));
  PuBu_Sequential_7_.add(color(166, 189, 219));
  PuBu_Sequential_7_.add(color(116, 169, 207));
  PuBu_Sequential_7_.add(color(54, 144, 192));
  PuBu_Sequential_7_.add(color(5, 112, 176));
  PuBu_Sequential_7_.add(color(3, 78, 123));
  return PuBu_Sequential_7_;
}


// color YlOrRd_Sequential_3
public ArrayList get_YlOrRd_Sequential_3() {
  ArrayList YlOrRd_Sequential_3_ = new ArrayList();
  YlOrRd_Sequential_3_.add(color(255, 237, 160));
  YlOrRd_Sequential_3_.add(color(254, 178, 76));
  YlOrRd_Sequential_3_.add(color(240, 59, 32));
  return YlOrRd_Sequential_3_;
}


// color PiYG_Diverging_3
public ArrayList get_PiYG_Diverging_3() {
  ArrayList PiYG_Diverging_3_ = new ArrayList();
  PiYG_Diverging_3_.add(color(233, 163, 201));
  PiYG_Diverging_3_.add(color(247, 247, 247));
  PiYG_Diverging_3_.add(color(161, 215, 106));
  return PiYG_Diverging_3_;
}


// color PiYG_Diverging_4
public ArrayList get_PiYG_Diverging_4() {
  ArrayList PiYG_Diverging_4_ = new ArrayList();
  PiYG_Diverging_4_.add(color(208, 28, 139));
  PiYG_Diverging_4_.add(color(241, 182, 218));
  PiYG_Diverging_4_.add(color(184, 225, 134));
  PiYG_Diverging_4_.add(color(77, 172, 38));
  return PiYG_Diverging_4_;
}


// color PiYG_Diverging_5
public ArrayList get_PiYG_Diverging_5() {
  ArrayList PiYG_Diverging_5_ = new ArrayList();
  PiYG_Diverging_5_.add(color(208, 28, 139));
  PiYG_Diverging_5_.add(color(241, 182, 218));
  PiYG_Diverging_5_.add(color(247, 247, 247));
  PiYG_Diverging_5_.add(color(184, 225, 134));
  PiYG_Diverging_5_.add(color(77, 172, 38));
  return PiYG_Diverging_5_;
}


// color PiYG_Diverging_6
public ArrayList get_PiYG_Diverging_6() {
  ArrayList PiYG_Diverging_6_ = new ArrayList();
  PiYG_Diverging_6_.add(color(197, 27, 125));
  PiYG_Diverging_6_.add(color(233, 163, 201));
  PiYG_Diverging_6_.add(color(253, 224, 239));
  PiYG_Diverging_6_.add(color(230, 245, 208));
  PiYG_Diverging_6_.add(color(161, 215, 106));
  PiYG_Diverging_6_.add(color(77, 146, 33));
  return PiYG_Diverging_6_;
}


// color PiYG_Diverging_7
public ArrayList get_PiYG_Diverging_7() {
  ArrayList PiYG_Diverging_7_ = new ArrayList();
  PiYG_Diverging_7_.add(color(197, 27, 125));
  PiYG_Diverging_7_.add(color(233, 163, 201));
  PiYG_Diverging_7_.add(color(253, 224, 239));
  PiYG_Diverging_7_.add(color(247, 247, 247));
  PiYG_Diverging_7_.add(color(230, 245, 208));
  PiYG_Diverging_7_.add(color(161, 215, 106));
  PiYG_Diverging_7_.add(color(77, 146, 33));
  return PiYG_Diverging_7_;
}


// color PiYG_Diverging_8
public ArrayList get_PiYG_Diverging_8() {
  ArrayList PiYG_Diverging_8_ = new ArrayList();
  PiYG_Diverging_8_.add(color(197, 27, 125));
  PiYG_Diverging_8_.add(color(222, 119, 174));
  PiYG_Diverging_8_.add(color(241, 182, 218));
  PiYG_Diverging_8_.add(color(253, 224, 239));
  PiYG_Diverging_8_.add(color(230, 245, 208));
  PiYG_Diverging_8_.add(color(184, 225, 134));
  PiYG_Diverging_8_.add(color(127, 188, 65));
  PiYG_Diverging_8_.add(color(77, 146, 33));
  return PiYG_Diverging_8_;
}


// color PiYG_Diverging_9
public ArrayList get_PiYG_Diverging_9() {
  ArrayList PiYG_Diverging_9_ = new ArrayList();
  PiYG_Diverging_9_.add(color(197, 27, 125));
  PiYG_Diverging_9_.add(color(222, 119, 174));
  PiYG_Diverging_9_.add(color(241, 182, 218));
  PiYG_Diverging_9_.add(color(253, 224, 239));
  PiYG_Diverging_9_.add(color(247, 247, 247));
  PiYG_Diverging_9_.add(color(230, 245, 208));
  PiYG_Diverging_9_.add(color(184, 225, 134));
  PiYG_Diverging_9_.add(color(127, 188, 65));
  PiYG_Diverging_9_.add(color(77, 146, 33));
  return PiYG_Diverging_9_;
}


// color YlOrBr_Sequential_6
public ArrayList get_YlOrBr_Sequential_6() {
  ArrayList YlOrBr_Sequential_6_ = new ArrayList();
  YlOrBr_Sequential_6_.add(color(255, 255, 212));
  YlOrBr_Sequential_6_.add(color(254, 227, 145));
  YlOrBr_Sequential_6_.add(color(254, 196, 79));
  YlOrBr_Sequential_6_.add(color(254, 153, 41));
  YlOrBr_Sequential_6_.add(color(217, 95, 14));
  YlOrBr_Sequential_6_.add(color(153, 52, 4));
  return YlOrBr_Sequential_6_;
}


// color Dark2_Qualitative_3
public ArrayList get_Dark2_Qualitative_3() {
  ArrayList Dark2_Qualitative_3_ = new ArrayList();
  Dark2_Qualitative_3_.add(color(27, 158, 119));
  Dark2_Qualitative_3_.add(color(217, 95, 2));
  Dark2_Qualitative_3_.add(color(117, 112, 179));
  return Dark2_Qualitative_3_;
}


// color Dark2_Qualitative_4
public ArrayList get_Dark2_Qualitative_4() {
  ArrayList Dark2_Qualitative_4_ = new ArrayList();
  Dark2_Qualitative_4_.add(color(27, 158, 119));
  Dark2_Qualitative_4_.add(color(217, 95, 2));
  Dark2_Qualitative_4_.add(color(117, 112, 179));
  Dark2_Qualitative_4_.add(color(231, 41, 138));
  return Dark2_Qualitative_4_;
}


// color Dark2_Qualitative_5
public ArrayList get_Dark2_Qualitative_5() {
  ArrayList Dark2_Qualitative_5_ = new ArrayList();
  Dark2_Qualitative_5_.add(color(27, 158, 119));
  Dark2_Qualitative_5_.add(color(217, 95, 2));
  Dark2_Qualitative_5_.add(color(117, 112, 179));
  Dark2_Qualitative_5_.add(color(231, 41, 138));
  Dark2_Qualitative_5_.add(color(102, 166, 30));
  return Dark2_Qualitative_5_;
}


// color Dark2_Qualitative_6
public ArrayList get_Dark2_Qualitative_6() {
  ArrayList Dark2_Qualitative_6_ = new ArrayList();
  Dark2_Qualitative_6_.add(color(27, 158, 119));
  Dark2_Qualitative_6_.add(color(217, 95, 2));
  Dark2_Qualitative_6_.add(color(117, 112, 179));
  Dark2_Qualitative_6_.add(color(231, 41, 138));
  Dark2_Qualitative_6_.add(color(102, 166, 30));
  Dark2_Qualitative_6_.add(color(230, 171, 2));
  return Dark2_Qualitative_6_;
}


// color Dark2_Qualitative_7
public ArrayList get_Dark2_Qualitative_7() {
  ArrayList Dark2_Qualitative_7_ = new ArrayList();
  Dark2_Qualitative_7_.add(color(27, 158, 119));
  Dark2_Qualitative_7_.add(color(217, 95, 2));
  Dark2_Qualitative_7_.add(color(117, 112, 179));
  Dark2_Qualitative_7_.add(color(231, 41, 138));
  Dark2_Qualitative_7_.add(color(102, 166, 30));
  Dark2_Qualitative_7_.add(color(230, 171, 2));
  Dark2_Qualitative_7_.add(color(166, 118, 29));
  return Dark2_Qualitative_7_;
}


// color Dark2_Qualitative_8
public ArrayList get_Dark2_Qualitative_8() {
  ArrayList Dark2_Qualitative_8_ = new ArrayList();
  Dark2_Qualitative_8_.add(color(27, 158, 119));
  Dark2_Qualitative_8_.add(color(217, 95, 2));
  Dark2_Qualitative_8_.add(color(117, 112, 179));
  Dark2_Qualitative_8_.add(color(231, 41, 138));
  Dark2_Qualitative_8_.add(color(102, 166, 30));
  Dark2_Qualitative_8_.add(color(230, 171, 2));
  Dark2_Qualitative_8_.add(color(166, 118, 29));
  Dark2_Qualitative_8_.add(color(102, 102, 102));
  return Dark2_Qualitative_8_;
}


// color Spectral_Diverging_10
public ArrayList get_Spectral_Diverging_10() {
  ArrayList Spectral_Diverging_10_ = new ArrayList();
  Spectral_Diverging_10_.add(color(158, 1, 66));
  Spectral_Diverging_10_.add(color(213, 62, 79));
  Spectral_Diverging_10_.add(color(244, 109, 67));
  Spectral_Diverging_10_.add(color(253, 174, 97));
  Spectral_Diverging_10_.add(color(254, 224, 139));
  Spectral_Diverging_10_.add(color(230, 245, 152));
  Spectral_Diverging_10_.add(color(171, 221, 164));
  Spectral_Diverging_10_.add(color(102, 194, 165));
  Spectral_Diverging_10_.add(color(50, 136, 189));
  Spectral_Diverging_10_.add(color(94, 79, 162));
  return Spectral_Diverging_10_;
}


// color Greens_Sequential_8
public ArrayList get_Greens_Sequential_8() {
  ArrayList Greens_Sequential_8_ = new ArrayList();
  Greens_Sequential_8_.add(color(247, 252, 245));
  Greens_Sequential_8_.add(color(229, 245, 224));
  Greens_Sequential_8_.add(color(199, 233, 192));
  Greens_Sequential_8_.add(color(161, 217, 155));
  Greens_Sequential_8_.add(color(116, 196, 118));
  Greens_Sequential_8_.add(color(65, 171, 93));
  Greens_Sequential_8_.add(color(35, 139, 69));
  Greens_Sequential_8_.add(color(0, 90, 50));
  return Greens_Sequential_8_;
}


// color Greens_Sequential_9
public ArrayList get_Greens_Sequential_9() {
  ArrayList Greens_Sequential_9_ = new ArrayList();
  Greens_Sequential_9_.add(color(247, 252, 245));
  Greens_Sequential_9_.add(color(229, 245, 224));
  Greens_Sequential_9_.add(color(199, 233, 192));
  Greens_Sequential_9_.add(color(161, 217, 155));
  Greens_Sequential_9_.add(color(116, 196, 118));
  Greens_Sequential_9_.add(color(65, 171, 93));
  Greens_Sequential_9_.add(color(35, 139, 69));
  Greens_Sequential_9_.add(color(0, 109, 44));
  Greens_Sequential_9_.add(color(0, 68, 27));
  return Greens_Sequential_9_;
}


// color YlGn_Sequential_8
public ArrayList get_YlGn_Sequential_8() {
  ArrayList YlGn_Sequential_8_ = new ArrayList();
  YlGn_Sequential_8_.add(color(255, 255, 229));
  YlGn_Sequential_8_.add(color(247, 252, 185));
  YlGn_Sequential_8_.add(color(217, 240, 163));
  YlGn_Sequential_8_.add(color(173, 221, 142));
  YlGn_Sequential_8_.add(color(120, 198, 121));
  YlGn_Sequential_8_.add(color(65, 171, 93));
  YlGn_Sequential_8_.add(color(35, 132, 67));
  YlGn_Sequential_8_.add(color(0, 90, 50));
  return YlGn_Sequential_8_;
}


// color PuBuGn_Sequential_6
public ArrayList get_PuBuGn_Sequential_6() {
  ArrayList PuBuGn_Sequential_6_ = new ArrayList();
  PuBuGn_Sequential_6_.add(color(246, 239, 247));
  PuBuGn_Sequential_6_.add(color(208, 209, 230));
  PuBuGn_Sequential_6_.add(color(166, 189, 219));
  PuBuGn_Sequential_6_.add(color(103, 169, 207));
  PuBuGn_Sequential_6_.add(color(28, 144, 153));
  PuBuGn_Sequential_6_.add(color(1, 108, 89));
  return PuBuGn_Sequential_6_;
}


// color Greens_Sequential_3
public ArrayList get_Greens_Sequential_3() {
  ArrayList Greens_Sequential_3_ = new ArrayList();
  Greens_Sequential_3_.add(color(229, 245, 224));
  Greens_Sequential_3_.add(color(161, 217, 155));
  Greens_Sequential_3_.add(color(49, 163, 84));
  return Greens_Sequential_3_;
}


// color Greens_Sequential_4
public ArrayList get_Greens_Sequential_4() {
  ArrayList Greens_Sequential_4_ = new ArrayList();
  Greens_Sequential_4_.add(color(237, 248, 233));
  Greens_Sequential_4_.add(color(186, 228, 179));
  Greens_Sequential_4_.add(color(116, 196, 118));
  Greens_Sequential_4_.add(color(35, 139, 69));
  return Greens_Sequential_4_;
}


// color Greens_Sequential_5
public ArrayList get_Greens_Sequential_5() {
  ArrayList Greens_Sequential_5_ = new ArrayList();
  Greens_Sequential_5_.add(color(237, 248, 233));
  Greens_Sequential_5_.add(color(186, 228, 179));
  Greens_Sequential_5_.add(color(116, 196, 118));
  Greens_Sequential_5_.add(color(49, 163, 84));
  Greens_Sequential_5_.add(color(0, 109, 44));
  return Greens_Sequential_5_;
}


// color Greens_Sequential_6
public ArrayList get_Greens_Sequential_6() {
  ArrayList Greens_Sequential_6_ = new ArrayList();
  Greens_Sequential_6_.add(color(237, 248, 233));
  Greens_Sequential_6_.add(color(199, 233, 192));
  Greens_Sequential_6_.add(color(161, 217, 155));
  Greens_Sequential_6_.add(color(116, 196, 118));
  Greens_Sequential_6_.add(color(49, 163, 84));
  Greens_Sequential_6_.add(color(0, 109, 44));
  return Greens_Sequential_6_;
}


// color Greens_Sequential_7
public ArrayList get_Greens_Sequential_7() {
  ArrayList Greens_Sequential_7_ = new ArrayList();
  Greens_Sequential_7_.add(color(237, 248, 233));
  Greens_Sequential_7_.add(color(199, 233, 192));
  Greens_Sequential_7_.add(color(161, 217, 155));
  Greens_Sequential_7_.add(color(116, 196, 118));
  Greens_Sequential_7_.add(color(65, 171, 93));
  Greens_Sequential_7_.add(color(35, 139, 69));
  Greens_Sequential_7_.add(color(0, 90, 50));
  return Greens_Sequential_7_;
}


// color YlOrRd_Sequential_9
public ArrayList get_YlOrRd_Sequential_9() {
  ArrayList YlOrRd_Sequential_9_ = new ArrayList();
  YlOrRd_Sequential_9_.add(color(255, 255, 204));
  YlOrRd_Sequential_9_.add(color(255, 237, 160));
  YlOrRd_Sequential_9_.add(color(254, 217, 118));
  YlOrRd_Sequential_9_.add(color(254, 178, 76));
  YlOrRd_Sequential_9_.add(color(253, 141, 60));
  YlOrRd_Sequential_9_.add(color(252, 78, 42));
  YlOrRd_Sequential_9_.add(color(227, 26, 28));
  YlOrRd_Sequential_9_.add(color(189, 0, 38));
  YlOrRd_Sequential_9_.add(color(128, 0, 38));
  return YlOrRd_Sequential_9_;
}


// color BuPu_Sequential_8
public ArrayList get_BuPu_Sequential_8() {
  ArrayList BuPu_Sequential_8_ = new ArrayList();
  BuPu_Sequential_8_.add(color(247, 252, 253));
  BuPu_Sequential_8_.add(color(224, 236, 244));
  BuPu_Sequential_8_.add(color(191, 211, 230));
  BuPu_Sequential_8_.add(color(158, 188, 218));
  BuPu_Sequential_8_.add(color(140, 150, 198));
  BuPu_Sequential_8_.add(color(140, 107, 177));
  BuPu_Sequential_8_.add(color(136, 65, 157));
  BuPu_Sequential_8_.add(color(110, 1, 107));
  return BuPu_Sequential_8_;
}


// color BuPu_Sequential_9
public ArrayList get_BuPu_Sequential_9() {
  ArrayList BuPu_Sequential_9_ = new ArrayList();
  BuPu_Sequential_9_.add(color(247, 252, 253));
  BuPu_Sequential_9_.add(color(224, 236, 244));
  BuPu_Sequential_9_.add(color(191, 211, 230));
  BuPu_Sequential_9_.add(color(158, 188, 218));
  BuPu_Sequential_9_.add(color(140, 150, 198));
  BuPu_Sequential_9_.add(color(140, 107, 177));
  BuPu_Sequential_9_.add(color(136, 65, 157));
  BuPu_Sequential_9_.add(color(129, 15, 124));
  BuPu_Sequential_9_.add(color(77, 0, 75));
  return BuPu_Sequential_9_;
}


// color BuPu_Sequential_4
public ArrayList get_BuPu_Sequential_4() {
  ArrayList BuPu_Sequential_4_ = new ArrayList();
  BuPu_Sequential_4_.add(color(237, 248, 251));
  BuPu_Sequential_4_.add(color(179, 205, 227));
  BuPu_Sequential_4_.add(color(140, 150, 198));
  BuPu_Sequential_4_.add(color(136, 65, 157));
  return BuPu_Sequential_4_;
}


// color BuPu_Sequential_5
public ArrayList get_BuPu_Sequential_5() {
  ArrayList BuPu_Sequential_5_ = new ArrayList();
  BuPu_Sequential_5_.add(color(237, 248, 251));
  BuPu_Sequential_5_.add(color(179, 205, 227));
  BuPu_Sequential_5_.add(color(140, 150, 198));
  BuPu_Sequential_5_.add(color(136, 86, 167));
  BuPu_Sequential_5_.add(color(129, 15, 124));
  return BuPu_Sequential_5_;
}


// color BuPu_Sequential_6
public ArrayList get_BuPu_Sequential_6() {
  ArrayList BuPu_Sequential_6_ = new ArrayList();
  BuPu_Sequential_6_.add(color(237, 248, 251));
  BuPu_Sequential_6_.add(color(191, 211, 230));
  BuPu_Sequential_6_.add(color(158, 188, 218));
  BuPu_Sequential_6_.add(color(140, 150, 198));
  BuPu_Sequential_6_.add(color(136, 86, 167));
  BuPu_Sequential_6_.add(color(129, 15, 124));
  return BuPu_Sequential_6_;
}


// color BuPu_Sequential_7
public ArrayList get_BuPu_Sequential_7() {
  ArrayList BuPu_Sequential_7_ = new ArrayList();
  BuPu_Sequential_7_.add(color(237, 248, 251));
  BuPu_Sequential_7_.add(color(191, 211, 230));
  BuPu_Sequential_7_.add(color(158, 188, 218));
  BuPu_Sequential_7_.add(color(140, 150, 198));
  BuPu_Sequential_7_.add(color(140, 107, 177));
  BuPu_Sequential_7_.add(color(136, 65, 157));
  BuPu_Sequential_7_.add(color(110, 1, 107));
  return BuPu_Sequential_7_;
}


// color BuPu_Sequential_3
public ArrayList get_BuPu_Sequential_3() {
  ArrayList BuPu_Sequential_3_ = new ArrayList();
  BuPu_Sequential_3_.add(color(224, 236, 244));
  BuPu_Sequential_3_.add(color(158, 188, 218));
  BuPu_Sequential_3_.add(color(136, 86, 167));
  return BuPu_Sequential_3_;
}


// color Set1_Qualitative_6
public ArrayList get_Set1_Qualitative_6() {
  ArrayList Set1_Qualitative_6_ = new ArrayList();
  Set1_Qualitative_6_.add(color(228, 26, 28));
  Set1_Qualitative_6_.add(color(55, 126, 184));
  Set1_Qualitative_6_.add(color(77, 175, 74));
  Set1_Qualitative_6_.add(color(152, 78, 163));
  Set1_Qualitative_6_.add(color(255, 127, 0));
  Set1_Qualitative_6_.add(color(255, 255, 51));
  return Set1_Qualitative_6_;
}


// color YlOrRd_Sequential_7
public ArrayList get_YlOrRd_Sequential_7() {
  ArrayList YlOrRd_Sequential_7_ = new ArrayList();
  YlOrRd_Sequential_7_.add(color(255, 255, 178));
  YlOrRd_Sequential_7_.add(color(254, 217, 118));
  YlOrRd_Sequential_7_.add(color(254, 178, 76));
  YlOrRd_Sequential_7_.add(color(253, 141, 60));
  YlOrRd_Sequential_7_.add(color(252, 78, 42));
  YlOrRd_Sequential_7_.add(color(227, 26, 28));
  YlOrRd_Sequential_7_.add(color(177, 0, 38));
  return YlOrRd_Sequential_7_;
}


// color YlOrRd_Sequential_4
public ArrayList get_YlOrRd_Sequential_4() {
  ArrayList YlOrRd_Sequential_4_ = new ArrayList();
  YlOrRd_Sequential_4_.add(color(255, 255, 178));
  YlOrRd_Sequential_4_.add(color(254, 204, 92));
  YlOrRd_Sequential_4_.add(color(253, 141, 60));
  YlOrRd_Sequential_4_.add(color(227, 26, 28));
  return YlOrRd_Sequential_4_;
}


// color YlOrRd_Sequential_5
public ArrayList get_YlOrRd_Sequential_5() {
  ArrayList YlOrRd_Sequential_5_ = new ArrayList();
  YlOrRd_Sequential_5_.add(color(255, 255, 178));
  YlOrRd_Sequential_5_.add(color(254, 204, 92));
  YlOrRd_Sequential_5_.add(color(253, 141, 60));
  YlOrRd_Sequential_5_.add(color(240, 59, 32));
  YlOrRd_Sequential_5_.add(color(189, 0, 38));
  return YlOrRd_Sequential_5_;
}


// color BrBG_Diverging_8
public ArrayList get_BrBG_Diverging_8() {
  ArrayList BrBG_Diverging_8_ = new ArrayList();
  BrBG_Diverging_8_.add(color(140, 81, 10));
  BrBG_Diverging_8_.add(color(191, 129, 45));
  BrBG_Diverging_8_.add(color(223, 194, 125));
  BrBG_Diverging_8_.add(color(246, 232, 195));
  BrBG_Diverging_8_.add(color(199, 234, 229));
  BrBG_Diverging_8_.add(color(128, 205, 193));
  BrBG_Diverging_8_.add(color(53, 151, 143));
  BrBG_Diverging_8_.add(color(1, 102, 94));
  return BrBG_Diverging_8_;
}


// color BrBG_Diverging_9
public ArrayList get_BrBG_Diverging_9() {
  ArrayList BrBG_Diverging_9_ = new ArrayList();
  BrBG_Diverging_9_.add(color(140, 81, 10));
  BrBG_Diverging_9_.add(color(191, 129, 45));
  BrBG_Diverging_9_.add(color(223, 194, 125));
  BrBG_Diverging_9_.add(color(246, 232, 195));
  BrBG_Diverging_9_.add(color(245, 245, 245));
  BrBG_Diverging_9_.add(color(199, 234, 229));
  BrBG_Diverging_9_.add(color(128, 205, 193));
  BrBG_Diverging_9_.add(color(53, 151, 143));
  BrBG_Diverging_9_.add(color(1, 102, 94));
  return BrBG_Diverging_9_;
}


// color BrBG_Diverging_4
public ArrayList get_BrBG_Diverging_4() {
  ArrayList BrBG_Diverging_4_ = new ArrayList();
  BrBG_Diverging_4_.add(color(166, 97, 26));
  BrBG_Diverging_4_.add(color(223, 194, 125));
  BrBG_Diverging_4_.add(color(128, 205, 193));
  BrBG_Diverging_4_.add(color(1, 133, 113));
  return BrBG_Diverging_4_;
}


// color BrBG_Diverging_5
public ArrayList get_BrBG_Diverging_5() {
  ArrayList BrBG_Diverging_5_ = new ArrayList();
  BrBG_Diverging_5_.add(color(166, 97, 26));
  BrBG_Diverging_5_.add(color(223, 194, 125));
  BrBG_Diverging_5_.add(color(245, 245, 245));
  BrBG_Diverging_5_.add(color(128, 205, 193));
  BrBG_Diverging_5_.add(color(1, 133, 113));
  return BrBG_Diverging_5_;
}


// color BrBG_Diverging_6
public ArrayList get_BrBG_Diverging_6() {
  ArrayList BrBG_Diverging_6_ = new ArrayList();
  BrBG_Diverging_6_.add(color(140, 81, 10));
  BrBG_Diverging_6_.add(color(216, 179, 101));
  BrBG_Diverging_6_.add(color(246, 232, 195));
  BrBG_Diverging_6_.add(color(199, 234, 229));
  BrBG_Diverging_6_.add(color(90, 180, 172));
  BrBG_Diverging_6_.add(color(1, 102, 94));
  return BrBG_Diverging_6_;
}


// color BrBG_Diverging_7
public ArrayList get_BrBG_Diverging_7() {
  ArrayList BrBG_Diverging_7_ = new ArrayList();
  BrBG_Diverging_7_.add(color(140, 81, 10));
  BrBG_Diverging_7_.add(color(216, 179, 101));
  BrBG_Diverging_7_.add(color(246, 232, 195));
  BrBG_Diverging_7_.add(color(245, 245, 245));
  BrBG_Diverging_7_.add(color(199, 234, 229));
  BrBG_Diverging_7_.add(color(90, 180, 172));
  BrBG_Diverging_7_.add(color(1, 102, 94));
  return BrBG_Diverging_7_;
}


// color YlOrRd_Sequential_8
public ArrayList get_YlOrRd_Sequential_8() {
  ArrayList YlOrRd_Sequential_8_ = new ArrayList();
  YlOrRd_Sequential_8_.add(color(255, 255, 204));
  YlOrRd_Sequential_8_.add(color(255, 237, 160));
  YlOrRd_Sequential_8_.add(color(254, 217, 118));
  YlOrRd_Sequential_8_.add(color(254, 178, 76));
  YlOrRd_Sequential_8_.add(color(253, 141, 60));
  YlOrRd_Sequential_8_.add(color(252, 78, 42));
  YlOrRd_Sequential_8_.add(color(227, 26, 28));
  YlOrRd_Sequential_8_.add(color(177, 0, 38));
  return YlOrRd_Sequential_8_;
}


// color BrBG_Diverging_3
public ArrayList get_BrBG_Diverging_3() {
  ArrayList BrBG_Diverging_3_ = new ArrayList();
  BrBG_Diverging_3_.add(color(216, 179, 101));
  BrBG_Diverging_3_.add(color(245, 245, 245));
  BrBG_Diverging_3_.add(color(90, 180, 172));
  return BrBG_Diverging_3_;
}


// color Spectral_Diverging_9
public ArrayList get_Spectral_Diverging_9() {
  ArrayList Spectral_Diverging_9_ = new ArrayList();
  Spectral_Diverging_9_.add(color(213, 62, 79));
  Spectral_Diverging_9_.add(color(244, 109, 67));
  Spectral_Diverging_9_.add(color(253, 174, 97));
  Spectral_Diverging_9_.add(color(254, 224, 139));
  Spectral_Diverging_9_.add(color(255, 255, 191));
  Spectral_Diverging_9_.add(color(230, 245, 152));
  Spectral_Diverging_9_.add(color(171, 221, 164));
  Spectral_Diverging_9_.add(color(102, 194, 165));
  Spectral_Diverging_9_.add(color(50, 136, 189));
  return Spectral_Diverging_9_;
}


// color Spectral_Diverging_8
public ArrayList get_Spectral_Diverging_8() {
  ArrayList Spectral_Diverging_8_ = new ArrayList();
  Spectral_Diverging_8_.add(color(213, 62, 79));
  Spectral_Diverging_8_.add(color(244, 109, 67));
  Spectral_Diverging_8_.add(color(253, 174, 97));
  Spectral_Diverging_8_.add(color(254, 224, 139));
  Spectral_Diverging_8_.add(color(230, 245, 152));
  Spectral_Diverging_8_.add(color(171, 221, 164));
  Spectral_Diverging_8_.add(color(102, 194, 165));
  Spectral_Diverging_8_.add(color(50, 136, 189));
  return Spectral_Diverging_8_;
}


// color PiYG_Diverging_10
public ArrayList get_PiYG_Diverging_10() {
  ArrayList PiYG_Diverging_10_ = new ArrayList();
  PiYG_Diverging_10_.add(color(142, 1, 82));
  PiYG_Diverging_10_.add(color(197, 27, 125));
  PiYG_Diverging_10_.add(color(222, 119, 174));
  PiYG_Diverging_10_.add(color(241, 182, 218));
  PiYG_Diverging_10_.add(color(253, 224, 239));
  PiYG_Diverging_10_.add(color(230, 245, 208));
  PiYG_Diverging_10_.add(color(184, 225, 134));
  PiYG_Diverging_10_.add(color(127, 188, 65));
  PiYG_Diverging_10_.add(color(77, 146, 33));
  PiYG_Diverging_10_.add(color(39, 100, 25));
  return PiYG_Diverging_10_;
}


// color PiYG_Diverging_11
public ArrayList get_PiYG_Diverging_11() {
  ArrayList PiYG_Diverging_11_ = new ArrayList();
  PiYG_Diverging_11_.add(color(142, 1, 82));
  PiYG_Diverging_11_.add(color(197, 27, 125));
  PiYG_Diverging_11_.add(color(222, 119, 174));
  PiYG_Diverging_11_.add(color(241, 182, 218));
  PiYG_Diverging_11_.add(color(253, 224, 239));
  PiYG_Diverging_11_.add(color(247, 247, 247));
  PiYG_Diverging_11_.add(color(230, 245, 208));
  PiYG_Diverging_11_.add(color(184, 225, 134));
  PiYG_Diverging_11_.add(color(127, 188, 65));
  PiYG_Diverging_11_.add(color(77, 146, 33));
  PiYG_Diverging_11_.add(color(39, 100, 25));
  return PiYG_Diverging_11_;
}


// color Spectral_Diverging_3
public ArrayList get_Spectral_Diverging_3() {
  ArrayList Spectral_Diverging_3_ = new ArrayList();
  Spectral_Diverging_3_.add(color(252, 141, 89));
  Spectral_Diverging_3_.add(color(255, 255, 191));
  Spectral_Diverging_3_.add(color(153, 213, 148));
  return Spectral_Diverging_3_;
}


// color RdPu_Sequential_6
public ArrayList get_RdPu_Sequential_6() {
  ArrayList RdPu_Sequential_6_ = new ArrayList();
  RdPu_Sequential_6_.add(color(254, 235, 226));
  RdPu_Sequential_6_.add(color(252, 197, 192));
  RdPu_Sequential_6_.add(color(250, 159, 181));
  RdPu_Sequential_6_.add(color(247, 104, 161));
  RdPu_Sequential_6_.add(color(197, 27, 138));
  RdPu_Sequential_6_.add(color(122, 1, 119));
  return RdPu_Sequential_6_;
}


// color Spectral_Diverging_5
public ArrayList get_Spectral_Diverging_5() {
  ArrayList Spectral_Diverging_5_ = new ArrayList();
  Spectral_Diverging_5_.add(color(215, 25, 28));
  Spectral_Diverging_5_.add(color(253, 174, 97));
  Spectral_Diverging_5_.add(color(255, 255, 191));
  Spectral_Diverging_5_.add(color(171, 221, 164));
  Spectral_Diverging_5_.add(color(43, 131, 186));
  return Spectral_Diverging_5_;
}


// color Spectral_Diverging_4
public ArrayList get_Spectral_Diverging_4() {
  ArrayList Spectral_Diverging_4_ = new ArrayList();
  Spectral_Diverging_4_.add(color(215, 25, 28));
  Spectral_Diverging_4_.add(color(253, 174, 97));
  Spectral_Diverging_4_.add(color(171, 221, 164));
  Spectral_Diverging_4_.add(color(43, 131, 186));
  return Spectral_Diverging_4_;
}


// color Spectral_Diverging_7
public ArrayList get_Spectral_Diverging_7() {
  ArrayList Spectral_Diverging_7_ = new ArrayList();
  Spectral_Diverging_7_.add(color(213, 62, 79));
  Spectral_Diverging_7_.add(color(252, 141, 89));
  Spectral_Diverging_7_.add(color(254, 224, 139));
  Spectral_Diverging_7_.add(color(255, 255, 191));
  Spectral_Diverging_7_.add(color(230, 245, 152));
  Spectral_Diverging_7_.add(color(153, 213, 148));
  Spectral_Diverging_7_.add(color(50, 136, 189));
  return Spectral_Diverging_7_;
}


// color Spectral_Diverging_6
public ArrayList get_Spectral_Diverging_6() {
  ArrayList Spectral_Diverging_6_ = new ArrayList();
  Spectral_Diverging_6_.add(color(213, 62, 79));
  Spectral_Diverging_6_.add(color(252, 141, 89));
  Spectral_Diverging_6_.add(color(254, 224, 139));
  Spectral_Diverging_6_.add(color(230, 245, 152));
  Spectral_Diverging_6_.add(color(153, 213, 148));
  Spectral_Diverging_6_.add(color(50, 136, 189));
  return Spectral_Diverging_6_;
}


// color Pastel1_Qualitative_8
public ArrayList get_Pastel1_Qualitative_8() {
  ArrayList Pastel1_Qualitative_8_ = new ArrayList();
  Pastel1_Qualitative_8_.add(color(251, 180, 174));
  Pastel1_Qualitative_8_.add(color(179, 205, 227));
  Pastel1_Qualitative_8_.add(color(204, 235, 197));
  Pastel1_Qualitative_8_.add(color(222, 203, 228));
  Pastel1_Qualitative_8_.add(color(254, 217, 166));
  Pastel1_Qualitative_8_.add(color(255, 255, 204));
  Pastel1_Qualitative_8_.add(color(229, 216, 189));
  Pastel1_Qualitative_8_.add(color(253, 218, 236));
  return Pastel1_Qualitative_8_;
}


// color Pastel1_Qualitative_9
public ArrayList get_Pastel1_Qualitative_9() {
  ArrayList Pastel1_Qualitative_9_ = new ArrayList();
  Pastel1_Qualitative_9_.add(color(251, 180, 174));
  Pastel1_Qualitative_9_.add(color(179, 205, 227));
  Pastel1_Qualitative_9_.add(color(204, 235, 197));
  Pastel1_Qualitative_9_.add(color(222, 203, 228));
  Pastel1_Qualitative_9_.add(color(254, 217, 166));
  Pastel1_Qualitative_9_.add(color(255, 255, 204));
  Pastel1_Qualitative_9_.add(color(229, 216, 189));
  Pastel1_Qualitative_9_.add(color(253, 218, 236));
  Pastel1_Qualitative_9_.add(color(242, 242, 242));
  return Pastel1_Qualitative_9_;
}


// color PuBu_Sequential_5
public ArrayList get_PuBu_Sequential_5() {
  ArrayList PuBu_Sequential_5_ = new ArrayList();
  PuBu_Sequential_5_.add(color(241, 238, 246));
  PuBu_Sequential_5_.add(color(189, 201, 225));
  PuBu_Sequential_5_.add(color(116, 169, 207));
  PuBu_Sequential_5_.add(color(43, 140, 190));
  PuBu_Sequential_5_.add(color(4, 90, 141));
  return PuBu_Sequential_5_;
}


// color PRGn_Diverging_10
public ArrayList get_PRGn_Diverging_10() {
  ArrayList PRGn_Diverging_10_ = new ArrayList();
  PRGn_Diverging_10_.add(color(64, 0, 75));
  PRGn_Diverging_10_.add(color(118, 42, 131));
  PRGn_Diverging_10_.add(color(153, 112, 171));
  PRGn_Diverging_10_.add(color(194, 165, 207));
  PRGn_Diverging_10_.add(color(231, 212, 232));
  PRGn_Diverging_10_.add(color(217, 240, 211));
  PRGn_Diverging_10_.add(color(166, 219, 160));
  PRGn_Diverging_10_.add(color(90, 174, 97));
  PRGn_Diverging_10_.add(color(27, 120, 55));
  PRGn_Diverging_10_.add(color(0, 68, 27));
  return PRGn_Diverging_10_;
}


// color PRGn_Diverging_11
public ArrayList get_PRGn_Diverging_11() {
  ArrayList PRGn_Diverging_11_ = new ArrayList();
  PRGn_Diverging_11_.add(color(64, 0, 75));
  PRGn_Diverging_11_.add(color(118, 42, 131));
  PRGn_Diverging_11_.add(color(153, 112, 171));
  PRGn_Diverging_11_.add(color(194, 165, 207));
  PRGn_Diverging_11_.add(color(231, 212, 232));
  PRGn_Diverging_11_.add(color(247, 247, 247));
  PRGn_Diverging_11_.add(color(217, 240, 211));
  PRGn_Diverging_11_.add(color(166, 219, 160));
  PRGn_Diverging_11_.add(color(90, 174, 97));
  PRGn_Diverging_11_.add(color(27, 120, 55));
  PRGn_Diverging_11_.add(color(0, 68, 27));
  return PRGn_Diverging_11_;
}


// color Pastel1_Qualitative_3
public ArrayList get_Pastel1_Qualitative_3() {
  ArrayList Pastel1_Qualitative_3_ = new ArrayList();
  Pastel1_Qualitative_3_.add(color(251, 180, 174));
  Pastel1_Qualitative_3_.add(color(179, 205, 227));
  Pastel1_Qualitative_3_.add(color(204, 235, 197));
  return Pastel1_Qualitative_3_;
}


// color Pastel1_Qualitative_4
public ArrayList get_Pastel1_Qualitative_4() {
  ArrayList Pastel1_Qualitative_4_ = new ArrayList();
  Pastel1_Qualitative_4_.add(color(251, 180, 174));
  Pastel1_Qualitative_4_.add(color(179, 205, 227));
  Pastel1_Qualitative_4_.add(color(204, 235, 197));
  Pastel1_Qualitative_4_.add(color(222, 203, 228));
  return Pastel1_Qualitative_4_;
}


// color Pastel1_Qualitative_5
public ArrayList get_Pastel1_Qualitative_5() {
  ArrayList Pastel1_Qualitative_5_ = new ArrayList();
  Pastel1_Qualitative_5_.add(color(251, 180, 174));
  Pastel1_Qualitative_5_.add(color(179, 205, 227));
  Pastel1_Qualitative_5_.add(color(204, 235, 197));
  Pastel1_Qualitative_5_.add(color(222, 203, 228));
  Pastel1_Qualitative_5_.add(color(254, 217, 166));
  return Pastel1_Qualitative_5_;
}


// color Pastel1_Qualitative_6
public ArrayList get_Pastel1_Qualitative_6() {
  ArrayList Pastel1_Qualitative_6_ = new ArrayList();
  Pastel1_Qualitative_6_.add(color(251, 180, 174));
  Pastel1_Qualitative_6_.add(color(179, 205, 227));
  Pastel1_Qualitative_6_.add(color(204, 235, 197));
  Pastel1_Qualitative_6_.add(color(222, 203, 228));
  Pastel1_Qualitative_6_.add(color(254, 217, 166));
  Pastel1_Qualitative_6_.add(color(255, 255, 204));
  return Pastel1_Qualitative_6_;
}


// color Pastel1_Qualitative_7
public ArrayList get_Pastel1_Qualitative_7() {
  ArrayList Pastel1_Qualitative_7_ = new ArrayList();
  Pastel1_Qualitative_7_.add(color(251, 180, 174));
  Pastel1_Qualitative_7_.add(color(179, 205, 227));
  Pastel1_Qualitative_7_.add(color(204, 235, 197));
  Pastel1_Qualitative_7_.add(color(222, 203, 228));
  Pastel1_Qualitative_7_.add(color(254, 217, 166));
  Pastel1_Qualitative_7_.add(color(255, 255, 204));
  Pastel1_Qualitative_7_.add(color(229, 216, 189));
  return Pastel1_Qualitative_7_;
}


// color YlOrBr_Sequential_8
public ArrayList get_YlOrBr_Sequential_8() {
  ArrayList YlOrBr_Sequential_8_ = new ArrayList();
  YlOrBr_Sequential_8_.add(color(255, 255, 229));
  YlOrBr_Sequential_8_.add(color(255, 247, 188));
  YlOrBr_Sequential_8_.add(color(254, 227, 145));
  YlOrBr_Sequential_8_.add(color(254, 196, 79));
  YlOrBr_Sequential_8_.add(color(254, 153, 41));
  YlOrBr_Sequential_8_.add(color(236, 112, 20));
  YlOrBr_Sequential_8_.add(color(204, 76, 2));
  YlOrBr_Sequential_8_.add(color(140, 45, 4));
  return YlOrBr_Sequential_8_;
}


// color YlOrBr_Sequential_9
public ArrayList get_YlOrBr_Sequential_9() {
  ArrayList YlOrBr_Sequential_9_ = new ArrayList();
  YlOrBr_Sequential_9_.add(color(255, 255, 229));
  YlOrBr_Sequential_9_.add(color(255, 247, 188));
  YlOrBr_Sequential_9_.add(color(254, 227, 145));
  YlOrBr_Sequential_9_.add(color(254, 196, 79));
  YlOrBr_Sequential_9_.add(color(254, 153, 41));
  YlOrBr_Sequential_9_.add(color(236, 112, 20));
  YlOrBr_Sequential_9_.add(color(204, 76, 2));
  YlOrBr_Sequential_9_.add(color(153, 52, 4));
  YlOrBr_Sequential_9_.add(color(102, 37, 6));
  return YlOrBr_Sequential_9_;
}


// color YlGnBu_Sequential_5
public ArrayList get_YlGnBu_Sequential_5() {
  ArrayList YlGnBu_Sequential_5_ = new ArrayList();
  YlGnBu_Sequential_5_.add(color(255, 255, 204));
  YlGnBu_Sequential_5_.add(color(161, 218, 180));
  YlGnBu_Sequential_5_.add(color(65, 182, 196));
  YlGnBu_Sequential_5_.add(color(44, 127, 184));
  YlGnBu_Sequential_5_.add(color(37, 52, 148));
  return YlGnBu_Sequential_5_;
}


// color YlOrBr_Sequential_3
public ArrayList get_YlOrBr_Sequential_3() {
  ArrayList YlOrBr_Sequential_3_ = new ArrayList();
  YlOrBr_Sequential_3_.add(color(255, 247, 188));
  YlOrBr_Sequential_3_.add(color(254, 196, 79));
  YlOrBr_Sequential_3_.add(color(217, 95, 14));
  return YlOrBr_Sequential_3_;
}


// color YlOrBr_Sequential_4
public ArrayList get_YlOrBr_Sequential_4() {
  ArrayList YlOrBr_Sequential_4_ = new ArrayList();
  YlOrBr_Sequential_4_.add(color(255, 255, 212));
  YlOrBr_Sequential_4_.add(color(254, 217, 142));
  YlOrBr_Sequential_4_.add(color(254, 153, 41));
  YlOrBr_Sequential_4_.add(color(204, 76, 2));
  return YlOrBr_Sequential_4_;
}


// color YlOrBr_Sequential_5
public ArrayList get_YlOrBr_Sequential_5() {
  ArrayList YlOrBr_Sequential_5_ = new ArrayList();
  YlOrBr_Sequential_5_.add(color(255, 255, 212));
  YlOrBr_Sequential_5_.add(color(254, 217, 142));
  YlOrBr_Sequential_5_.add(color(254, 153, 41));
  YlOrBr_Sequential_5_.add(color(217, 95, 14));
  YlOrBr_Sequential_5_.add(color(153, 52, 4));
  return YlOrBr_Sequential_5_;
}


// color Set3_Qualitative_8
public ArrayList get_Set3_Qualitative_8() {
  ArrayList Set3_Qualitative_8_ = new ArrayList();
  Set3_Qualitative_8_.add(color(141, 211, 199));
  Set3_Qualitative_8_.add(color(255, 255, 179));
  Set3_Qualitative_8_.add(color(190, 186, 218));
  Set3_Qualitative_8_.add(color(251, 128, 114));
  Set3_Qualitative_8_.add(color(128, 177, 211));
  Set3_Qualitative_8_.add(color(253, 180, 98));
  Set3_Qualitative_8_.add(color(179, 222, 105));
  Set3_Qualitative_8_.add(color(252, 205, 229));
  return Set3_Qualitative_8_;
}


// color YlOrBr_Sequential_7
public ArrayList get_YlOrBr_Sequential_7() {
  ArrayList YlOrBr_Sequential_7_ = new ArrayList();
  YlOrBr_Sequential_7_.add(color(255, 255, 212));
  YlOrBr_Sequential_7_.add(color(254, 227, 145));
  YlOrBr_Sequential_7_.add(color(254, 196, 79));
  YlOrBr_Sequential_7_.add(color(254, 153, 41));
  YlOrBr_Sequential_7_.add(color(236, 112, 20));
  YlOrBr_Sequential_7_.add(color(204, 76, 2));
  YlOrBr_Sequential_7_.add(color(140, 45, 4));
  return YlOrBr_Sequential_7_;
}


// color RdBu_Diverging_10
public ArrayList get_RdBu_Diverging_10() {
  ArrayList RdBu_Diverging_10_ = new ArrayList();
  RdBu_Diverging_10_.add(color(103, 0, 31));
  RdBu_Diverging_10_.add(color(178, 24, 43));
  RdBu_Diverging_10_.add(color(214, 96, 77));
  RdBu_Diverging_10_.add(color(244, 165, 130));
  RdBu_Diverging_10_.add(color(253, 219, 199));
  RdBu_Diverging_10_.add(color(209, 229, 240));
  RdBu_Diverging_10_.add(color(146, 197, 222));
  RdBu_Diverging_10_.add(color(67, 147, 195));
  RdBu_Diverging_10_.add(color(33, 102, 172));
  RdBu_Diverging_10_.add(color(5, 48, 97));
  return RdBu_Diverging_10_;
}


// color RdBu_Diverging_11
public ArrayList get_RdBu_Diverging_11() {
  ArrayList RdBu_Diverging_11_ = new ArrayList();
  RdBu_Diverging_11_.add(color(103, 0, 31));
  RdBu_Diverging_11_.add(color(178, 24, 43));
  RdBu_Diverging_11_.add(color(214, 96, 77));
  RdBu_Diverging_11_.add(color(244, 165, 130));
  RdBu_Diverging_11_.add(color(253, 219, 199));
  RdBu_Diverging_11_.add(color(247, 247, 247));
  RdBu_Diverging_11_.add(color(209, 229, 240));
  RdBu_Diverging_11_.add(color(146, 197, 222));
  RdBu_Diverging_11_.add(color(67, 147, 195));
  RdBu_Diverging_11_.add(color(33, 102, 172));
  RdBu_Diverging_11_.add(color(5, 48, 97));
  return RdBu_Diverging_11_;
}


// color Paired_Qualitative_10
public ArrayList get_Paired_Qualitative_10() {
  ArrayList Paired_Qualitative_10_ = new ArrayList();
  Paired_Qualitative_10_.add(color(166, 206, 227));
  Paired_Qualitative_10_.add(color(31, 120, 180));
  Paired_Qualitative_10_.add(color(178, 223, 138));
  Paired_Qualitative_10_.add(color(51, 160, 44));
  Paired_Qualitative_10_.add(color(251, 154, 153));
  Paired_Qualitative_10_.add(color(227, 26, 28));
  Paired_Qualitative_10_.add(color(253, 191, 111));
  Paired_Qualitative_10_.add(color(255, 127, 0));
  Paired_Qualitative_10_.add(color(202, 178, 214));
  Paired_Qualitative_10_.add(color(106, 61, 154));
  return Paired_Qualitative_10_;
}


// color Paired_Qualitative_11
public ArrayList get_Paired_Qualitative_11() {
  ArrayList Paired_Qualitative_11_ = new ArrayList();
  Paired_Qualitative_11_.add(color(166, 206, 227));
  Paired_Qualitative_11_.add(color(31, 120, 180));
  Paired_Qualitative_11_.add(color(178, 223, 138));
  Paired_Qualitative_11_.add(color(51, 160, 44));
  Paired_Qualitative_11_.add(color(251, 154, 153));
  Paired_Qualitative_11_.add(color(227, 26, 28));
  Paired_Qualitative_11_.add(color(253, 191, 111));
  Paired_Qualitative_11_.add(color(255, 127, 0));
  Paired_Qualitative_11_.add(color(202, 178, 214));
  Paired_Qualitative_11_.add(color(106, 61, 154));
  Paired_Qualitative_11_.add(color(255, 255, 153));
  return Paired_Qualitative_11_;
}


// color Paired_Qualitative_12
public ArrayList get_Paired_Qualitative_12() {
  ArrayList Paired_Qualitative_12_ = new ArrayList();
  Paired_Qualitative_12_.add(color(166, 206, 227));
  Paired_Qualitative_12_.add(color(31, 120, 180));
  Paired_Qualitative_12_.add(color(178, 223, 138));
  Paired_Qualitative_12_.add(color(51, 160, 44));
  Paired_Qualitative_12_.add(color(251, 154, 153));
  Paired_Qualitative_12_.add(color(227, 26, 28));
  Paired_Qualitative_12_.add(color(253, 191, 111));
  Paired_Qualitative_12_.add(color(255, 127, 0));
  Paired_Qualitative_12_.add(color(202, 178, 214));
  Paired_Qualitative_12_.add(color(106, 61, 154));
  Paired_Qualitative_12_.add(color(255, 255, 153));
  Paired_Qualitative_12_.add(color(177, 89, 40));
  return Paired_Qualitative_12_;
}


// color RdYlGn_Diverging_4
public ArrayList get_RdYlGn_Diverging_4() {
  ArrayList RdYlGn_Diverging_4_ = new ArrayList();
  RdYlGn_Diverging_4_.add(color(215, 25, 28));
  RdYlGn_Diverging_4_.add(color(253, 174, 97));
  RdYlGn_Diverging_4_.add(color(166, 217, 106));
  RdYlGn_Diverging_4_.add(color(26, 150, 65));
  return RdYlGn_Diverging_4_;
}


// color YlGn_Sequential_5
public ArrayList get_YlGn_Sequential_5() {
  ArrayList YlGn_Sequential_5_ = new ArrayList();
  YlGn_Sequential_5_.add(color(255, 255, 204));
  YlGn_Sequential_5_.add(color(194, 230, 153));
  YlGn_Sequential_5_.add(color(120, 198, 121));
  YlGn_Sequential_5_.add(color(49, 163, 84));
  YlGn_Sequential_5_.add(color(0, 104, 55));
  return YlGn_Sequential_5_;
}


// color RdPu_Sequential_7
public ArrayList get_RdPu_Sequential_7() {
  ArrayList RdPu_Sequential_7_ = new ArrayList();
  RdPu_Sequential_7_.add(color(254, 235, 226));
  RdPu_Sequential_7_.add(color(252, 197, 192));
  RdPu_Sequential_7_.add(color(250, 159, 181));
  RdPu_Sequential_7_.add(color(247, 104, 161));
  RdPu_Sequential_7_.add(color(221, 52, 151));
  RdPu_Sequential_7_.add(color(174, 1, 126));
  RdPu_Sequential_7_.add(color(122, 1, 119));
  return RdPu_Sequential_7_;
}


// color Set3_Qualitative_9
public ArrayList get_Set3_Qualitative_9() {
  ArrayList Set3_Qualitative_9_ = new ArrayList();
  Set3_Qualitative_9_.add(color(141, 211, 199));
  Set3_Qualitative_9_.add(color(255, 255, 179));
  Set3_Qualitative_9_.add(color(190, 186, 218));
  Set3_Qualitative_9_.add(color(251, 128, 114));
  Set3_Qualitative_9_.add(color(128, 177, 211));
  Set3_Qualitative_9_.add(color(253, 180, 98));
  Set3_Qualitative_9_.add(color(179, 222, 105));
  Set3_Qualitative_9_.add(color(252, 205, 229));
  Set3_Qualitative_9_.add(color(217, 217, 217));
  return Set3_Qualitative_9_;
}


// color PuOr_Diverging_11
public ArrayList get_PuOr_Diverging_11() {
  ArrayList PuOr_Diverging_11_ = new ArrayList();
  PuOr_Diverging_11_.add(color(127, 59, 8));
  PuOr_Diverging_11_.add(color(179, 88, 6));
  PuOr_Diverging_11_.add(color(224, 130, 20));
  PuOr_Diverging_11_.add(color(253, 184, 99));
  PuOr_Diverging_11_.add(color(254, 224, 182));
  PuOr_Diverging_11_.add(color(247, 247, 247));
  PuOr_Diverging_11_.add(color(216, 218, 235));
  PuOr_Diverging_11_.add(color(178, 171, 210));
  PuOr_Diverging_11_.add(color(128, 115, 172));
  PuOr_Diverging_11_.add(color(84, 39, 136));
  PuOr_Diverging_11_.add(color(45, 0, 75));
  return PuOr_Diverging_11_;
}


// color PuOr_Diverging_10
public ArrayList get_PuOr_Diverging_10() {
  ArrayList PuOr_Diverging_10_ = new ArrayList();
  PuOr_Diverging_10_.add(color(127, 59, 8));
  PuOr_Diverging_10_.add(color(179, 88, 6));
  PuOr_Diverging_10_.add(color(224, 130, 20));
  PuOr_Diverging_10_.add(color(253, 184, 99));
  PuOr_Diverging_10_.add(color(254, 224, 182));
  PuOr_Diverging_10_.add(color(216, 218, 235));
  PuOr_Diverging_10_.add(color(178, 171, 210));
  PuOr_Diverging_10_.add(color(128, 115, 172));
  PuOr_Diverging_10_.add(color(84, 39, 136));
  PuOr_Diverging_10_.add(color(45, 0, 75));
  return PuOr_Diverging_10_;
}


// color RdPu_Sequential_5
public ArrayList get_RdPu_Sequential_5() {
  ArrayList RdPu_Sequential_5_ = new ArrayList();
  RdPu_Sequential_5_.add(color(254, 235, 226));
  RdPu_Sequential_5_.add(color(251, 180, 185));
  RdPu_Sequential_5_.add(color(247, 104, 161));
  RdPu_Sequential_5_.add(color(197, 27, 138));
  RdPu_Sequential_5_.add(color(122, 1, 119));
  return RdPu_Sequential_5_;
}


// color RdPu_Sequential_4
public ArrayList get_RdPu_Sequential_4() {
  ArrayList RdPu_Sequential_4_ = new ArrayList();
  RdPu_Sequential_4_.add(color(254, 235, 226));
  RdPu_Sequential_4_.add(color(251, 180, 185));
  RdPu_Sequential_4_.add(color(247, 104, 161));
  RdPu_Sequential_4_.add(color(174, 1, 126));
  return RdPu_Sequential_4_;
}


// color Paired_Qualitative_6
public ArrayList get_Paired_Qualitative_6() {
  ArrayList Paired_Qualitative_6_ = new ArrayList();
  Paired_Qualitative_6_.add(color(166, 206, 227));
  Paired_Qualitative_6_.add(color(31, 120, 180));
  Paired_Qualitative_6_.add(color(178, 223, 138));
  Paired_Qualitative_6_.add(color(51, 160, 44));
  Paired_Qualitative_6_.add(color(251, 154, 153));
  Paired_Qualitative_6_.add(color(227, 26, 28));
  return Paired_Qualitative_6_;
}


// color Paired_Qualitative_7
public ArrayList get_Paired_Qualitative_7() {
  ArrayList Paired_Qualitative_7_ = new ArrayList();
  Paired_Qualitative_7_.add(color(166, 206, 227));
  Paired_Qualitative_7_.add(color(31, 120, 180));
  Paired_Qualitative_7_.add(color(178, 223, 138));
  Paired_Qualitative_7_.add(color(51, 160, 44));
  Paired_Qualitative_7_.add(color(251, 154, 153));
  Paired_Qualitative_7_.add(color(227, 26, 28));
  Paired_Qualitative_7_.add(color(253, 191, 111));
  return Paired_Qualitative_7_;
}


// color Paired_Qualitative_4
public ArrayList get_Paired_Qualitative_4() {
  ArrayList Paired_Qualitative_4_ = new ArrayList();
  Paired_Qualitative_4_.add(color(166, 206, 227));
  Paired_Qualitative_4_.add(color(31, 120, 180));
  Paired_Qualitative_4_.add(color(178, 223, 138));
  Paired_Qualitative_4_.add(color(51, 160, 44));
  return Paired_Qualitative_4_;
}


// color Paired_Qualitative_5
public ArrayList get_Paired_Qualitative_5() {
  ArrayList Paired_Qualitative_5_ = new ArrayList();
  Paired_Qualitative_5_.add(color(166, 206, 227));
  Paired_Qualitative_5_.add(color(31, 120, 180));
  Paired_Qualitative_5_.add(color(178, 223, 138));
  Paired_Qualitative_5_.add(color(51, 160, 44));
  Paired_Qualitative_5_.add(color(251, 154, 153));
  return Paired_Qualitative_5_;
}


// color Paired_Qualitative_3
public ArrayList get_Paired_Qualitative_3() {
  ArrayList Paired_Qualitative_3_ = new ArrayList();
  Paired_Qualitative_3_.add(color(166, 206, 227));
  Paired_Qualitative_3_.add(color(31, 120, 180));
  Paired_Qualitative_3_.add(color(178, 223, 138));
  return Paired_Qualitative_3_;
}


// color RdYlGn_Diverging_7
public ArrayList get_RdYlGn_Diverging_7() {
  ArrayList RdYlGn_Diverging_7_ = new ArrayList();
  RdYlGn_Diverging_7_.add(color(215, 48, 39));
  RdYlGn_Diverging_7_.add(color(252, 141, 89));
  RdYlGn_Diverging_7_.add(color(254, 224, 139));
  RdYlGn_Diverging_7_.add(color(255, 255, 191));
  RdYlGn_Diverging_7_.add(color(217, 239, 139));
  RdYlGn_Diverging_7_.add(color(145, 207, 96));
  RdYlGn_Diverging_7_.add(color(26, 152, 80));
  return RdYlGn_Diverging_7_;
}


// color Paired_Qualitative_8
public ArrayList get_Paired_Qualitative_8() {
  ArrayList Paired_Qualitative_8_ = new ArrayList();
  Paired_Qualitative_8_.add(color(166, 206, 227));
  Paired_Qualitative_8_.add(color(31, 120, 180));
  Paired_Qualitative_8_.add(color(178, 223, 138));
  Paired_Qualitative_8_.add(color(51, 160, 44));
  Paired_Qualitative_8_.add(color(251, 154, 153));
  Paired_Qualitative_8_.add(color(227, 26, 28));
  Paired_Qualitative_8_.add(color(253, 191, 111));
  Paired_Qualitative_8_.add(color(255, 127, 0));
  return Paired_Qualitative_8_;
}


// color Paired_Qualitative_9
public ArrayList get_Paired_Qualitative_9() {
  ArrayList Paired_Qualitative_9_ = new ArrayList();
  Paired_Qualitative_9_.add(color(166, 206, 227));
  Paired_Qualitative_9_.add(color(31, 120, 180));
  Paired_Qualitative_9_.add(color(178, 223, 138));
  Paired_Qualitative_9_.add(color(51, 160, 44));
  Paired_Qualitative_9_.add(color(251, 154, 153));
  Paired_Qualitative_9_.add(color(227, 26, 28));
  Paired_Qualitative_9_.add(color(253, 191, 111));
  Paired_Qualitative_9_.add(color(255, 127, 0));
  Paired_Qualitative_9_.add(color(202, 178, 214));
  return Paired_Qualitative_9_;
}


// color PRGn_Diverging_5
public ArrayList get_PRGn_Diverging_5() {
  ArrayList PRGn_Diverging_5_ = new ArrayList();
  PRGn_Diverging_5_.add(color(123, 50, 148));
  PRGn_Diverging_5_.add(color(194, 165, 207));
  PRGn_Diverging_5_.add(color(247, 247, 247));
  PRGn_Diverging_5_.add(color(166, 219, 160));
  PRGn_Diverging_5_.add(color(0, 136, 55));
  return PRGn_Diverging_5_;
}


// color RdYlBu_Diverging_11
public ArrayList get_RdYlBu_Diverging_11() {
  ArrayList RdYlBu_Diverging_11_ = new ArrayList();
  RdYlBu_Diverging_11_.add(color(165, 0, 38));
  RdYlBu_Diverging_11_.add(color(215, 48, 39));
  RdYlBu_Diverging_11_.add(color(244, 109, 67));
  RdYlBu_Diverging_11_.add(color(253, 174, 97));
  RdYlBu_Diverging_11_.add(color(254, 224, 144));
  RdYlBu_Diverging_11_.add(color(255, 255, 191));
  RdYlBu_Diverging_11_.add(color(224, 243, 248));
  RdYlBu_Diverging_11_.add(color(171, 217, 233));
  RdYlBu_Diverging_11_.add(color(116, 173, 209));
  RdYlBu_Diverging_11_.add(color(69, 117, 180));
  RdYlBu_Diverging_11_.add(color(49, 54, 149));
  return RdYlBu_Diverging_11_;
}


// color YlGn_Sequential_6
public ArrayList get_YlGn_Sequential_6() {
  ArrayList YlGn_Sequential_6_ = new ArrayList();
  YlGn_Sequential_6_.add(color(255, 255, 204));
  YlGn_Sequential_6_.add(color(217, 240, 163));
  YlGn_Sequential_6_.add(color(173, 221, 142));
  YlGn_Sequential_6_.add(color(120, 198, 121));
  YlGn_Sequential_6_.add(color(49, 163, 84));
  YlGn_Sequential_6_.add(color(0, 104, 55));
  return YlGn_Sequential_6_;
}


// color RdYlGn_Diverging_6
public ArrayList get_RdYlGn_Diverging_6() {
  ArrayList RdYlGn_Diverging_6_ = new ArrayList();
  RdYlGn_Diverging_6_.add(color(215, 48, 39));
  RdYlGn_Diverging_6_.add(color(252, 141, 89));
  RdYlGn_Diverging_6_.add(color(254, 224, 139));
  RdYlGn_Diverging_6_.add(color(217, 239, 139));
  RdYlGn_Diverging_6_.add(color(145, 207, 96));
  RdYlGn_Diverging_6_.add(color(26, 152, 80));
  return RdYlGn_Diverging_6_;
}


// color Set1_Qualitative_7
public ArrayList get_Set1_Qualitative_7() {
  ArrayList Set1_Qualitative_7_ = new ArrayList();
  Set1_Qualitative_7_.add(color(228, 26, 28));
  Set1_Qualitative_7_.add(color(55, 126, 184));
  Set1_Qualitative_7_.add(color(77, 175, 74));
  Set1_Qualitative_7_.add(color(152, 78, 163));
  Set1_Qualitative_7_.add(color(255, 127, 0));
  Set1_Qualitative_7_.add(color(255, 255, 51));
  Set1_Qualitative_7_.add(color(166, 86, 40));
  return Set1_Qualitative_7_;
}


// color RdGy_Diverging_3
public ArrayList get_RdGy_Diverging_3() {
  ArrayList RdGy_Diverging_3_ = new ArrayList();
  RdGy_Diverging_3_.add(color(239, 138, 98));
  RdGy_Diverging_3_.add(color(255, 255, 255));
  RdGy_Diverging_3_.add(color(153, 153, 153));
  return RdGy_Diverging_3_;
}


// color Spectral_Diverging_11
public ArrayList get_Spectral_Diverging_11() {
  ArrayList Spectral_Diverging_11_ = new ArrayList();
  Spectral_Diverging_11_.add(color(158, 1, 66));
  Spectral_Diverging_11_.add(color(213, 62, 79));
  Spectral_Diverging_11_.add(color(244, 109, 67));
  Spectral_Diverging_11_.add(color(253, 174, 97));
  Spectral_Diverging_11_.add(color(254, 224, 139));
  Spectral_Diverging_11_.add(color(255, 255, 191));
  Spectral_Diverging_11_.add(color(230, 245, 152));
  Spectral_Diverging_11_.add(color(171, 221, 164));
  Spectral_Diverging_11_.add(color(102, 194, 165));
  Spectral_Diverging_11_.add(color(50, 136, 189));
  Spectral_Diverging_11_.add(color(94, 79, 162));
  return Spectral_Diverging_11_;
}


// color RdGy_Diverging_7
public ArrayList get_RdGy_Diverging_7() {
  ArrayList RdGy_Diverging_7_ = new ArrayList();
  RdGy_Diverging_7_.add(color(178, 24, 43));
  RdGy_Diverging_7_.add(color(239, 138, 98));
  RdGy_Diverging_7_.add(color(253, 219, 199));
  RdGy_Diverging_7_.add(color(255, 255, 255));
  RdGy_Diverging_7_.add(color(224, 224, 224));
  RdGy_Diverging_7_.add(color(153, 153, 153));
  RdGy_Diverging_7_.add(color(77, 77, 77));
  return RdGy_Diverging_7_;
}


// color RdGy_Diverging_6
public ArrayList get_RdGy_Diverging_6() {
  ArrayList RdGy_Diverging_6_ = new ArrayList();
  RdGy_Diverging_6_.add(color(178, 24, 43));
  RdGy_Diverging_6_.add(color(239, 138, 98));
  RdGy_Diverging_6_.add(color(253, 219, 199));
  RdGy_Diverging_6_.add(color(224, 224, 224));
  RdGy_Diverging_6_.add(color(153, 153, 153));
  RdGy_Diverging_6_.add(color(77, 77, 77));
  return RdGy_Diverging_6_;
}


// color RdGy_Diverging_5
public ArrayList get_RdGy_Diverging_5() {
  ArrayList RdGy_Diverging_5_ = new ArrayList();
  RdGy_Diverging_5_.add(color(202, 0, 32));
  RdGy_Diverging_5_.add(color(244, 165, 130));
  RdGy_Diverging_5_.add(color(255, 255, 255));
  RdGy_Diverging_5_.add(color(186, 186, 186));
  RdGy_Diverging_5_.add(color(64, 64, 64));
  return RdGy_Diverging_5_;
}


// color RdGy_Diverging_4
public ArrayList get_RdGy_Diverging_4() {
  ArrayList RdGy_Diverging_4_ = new ArrayList();
  RdGy_Diverging_4_.add(color(202, 0, 32));
  RdGy_Diverging_4_.add(color(244, 165, 130));
  RdGy_Diverging_4_.add(color(186, 186, 186));
  RdGy_Diverging_4_.add(color(64, 64, 64));
  return RdGy_Diverging_4_;
}


// color RdGy_Diverging_9
public ArrayList get_RdGy_Diverging_9() {
  ArrayList RdGy_Diverging_9_ = new ArrayList();
  RdGy_Diverging_9_.add(color(178, 24, 43));
  RdGy_Diverging_9_.add(color(214, 96, 77));
  RdGy_Diverging_9_.add(color(244, 165, 130));
  RdGy_Diverging_9_.add(color(253, 219, 199));
  RdGy_Diverging_9_.add(color(255, 255, 255));
  RdGy_Diverging_9_.add(color(224, 224, 224));
  RdGy_Diverging_9_.add(color(186, 186, 186));
  RdGy_Diverging_9_.add(color(135, 135, 135));
  RdGy_Diverging_9_.add(color(77, 77, 77));
  return RdGy_Diverging_9_;
}


// color RdGy_Diverging_8
public ArrayList get_RdGy_Diverging_8() {
  ArrayList RdGy_Diverging_8_ = new ArrayList();
  RdGy_Diverging_8_.add(color(178, 24, 43));
  RdGy_Diverging_8_.add(color(214, 96, 77));
  RdGy_Diverging_8_.add(color(244, 165, 130));
  RdGy_Diverging_8_.add(color(253, 219, 199));
  RdGy_Diverging_8_.add(color(224, 224, 224));
  RdGy_Diverging_8_.add(color(186, 186, 186));
  RdGy_Diverging_8_.add(color(135, 135, 135));
  RdGy_Diverging_8_.add(color(77, 77, 77));
  return RdGy_Diverging_8_;
}


// color RdYlGn_Diverging_11
public ArrayList get_RdYlGn_Diverging_11() {
  ArrayList RdYlGn_Diverging_11_ = new ArrayList();
  RdYlGn_Diverging_11_.add(color(165, 0, 38));
  RdYlGn_Diverging_11_.add(color(215, 48, 39));
  RdYlGn_Diverging_11_.add(color(244, 109, 67));
  RdYlGn_Diverging_11_.add(color(253, 174, 97));
  RdYlGn_Diverging_11_.add(color(254, 224, 139));
  RdYlGn_Diverging_11_.add(color(255, 255, 191));
  RdYlGn_Diverging_11_.add(color(217, 239, 139));
  RdYlGn_Diverging_11_.add(color(166, 217, 106));
  RdYlGn_Diverging_11_.add(color(102, 189, 99));
  RdYlGn_Diverging_11_.add(color(26, 152, 80));
  RdYlGn_Diverging_11_.add(color(0, 104, 55));
  return RdYlGn_Diverging_11_;
}


// color RdYlGn_Diverging_10
public ArrayList get_RdYlGn_Diverging_10() {
  ArrayList RdYlGn_Diverging_10_ = new ArrayList();
  RdYlGn_Diverging_10_.add(color(165, 0, 38));
  RdYlGn_Diverging_10_.add(color(215, 48, 39));
  RdYlGn_Diverging_10_.add(color(244, 109, 67));
  RdYlGn_Diverging_10_.add(color(253, 174, 97));
  RdYlGn_Diverging_10_.add(color(254, 224, 139));
  RdYlGn_Diverging_10_.add(color(217, 239, 139));
  RdYlGn_Diverging_10_.add(color(166, 217, 106));
  RdYlGn_Diverging_10_.add(color(102, 189, 99));
  RdYlGn_Diverging_10_.add(color(26, 152, 80));
  RdYlGn_Diverging_10_.add(color(0, 104, 55));
  return RdYlGn_Diverging_10_;
}


// color YlGn_Sequential_9
public ArrayList get_YlGn_Sequential_9() {
  ArrayList YlGn_Sequential_9_ = new ArrayList();
  YlGn_Sequential_9_.add(color(255, 255, 229));
  YlGn_Sequential_9_.add(color(247, 252, 185));
  YlGn_Sequential_9_.add(color(217, 240, 163));
  YlGn_Sequential_9_.add(color(173, 221, 142));
  YlGn_Sequential_9_.add(color(120, 198, 121));
  YlGn_Sequential_9_.add(color(65, 171, 93));
  YlGn_Sequential_9_.add(color(35, 132, 67));
  YlGn_Sequential_9_.add(color(0, 104, 55));
  YlGn_Sequential_9_.add(color(0, 69, 41));
  return YlGn_Sequential_9_;
}


// color RdYlBu_Diverging_9
public ArrayList get_RdYlBu_Diverging_9() {
  ArrayList RdYlBu_Diverging_9_ = new ArrayList();
  RdYlBu_Diverging_9_.add(color(215, 48, 39));
  RdYlBu_Diverging_9_.add(color(244, 109, 67));
  RdYlBu_Diverging_9_.add(color(253, 174, 97));
  RdYlBu_Diverging_9_.add(color(254, 224, 144));
  RdYlBu_Diverging_9_.add(color(255, 255, 191));
  RdYlBu_Diverging_9_.add(color(224, 243, 248));
  RdYlBu_Diverging_9_.add(color(171, 217, 233));
  RdYlBu_Diverging_9_.add(color(116, 173, 209));
  RdYlBu_Diverging_9_.add(color(69, 117, 180));
  return RdYlBu_Diverging_9_;
}


// color RdGy_Diverging_11
public ArrayList get_RdGy_Diverging_11() {
  ArrayList RdGy_Diverging_11_ = new ArrayList();
  RdGy_Diverging_11_.add(color(103, 0, 31));
  RdGy_Diverging_11_.add(color(178, 24, 43));
  RdGy_Diverging_11_.add(color(214, 96, 77));
  RdGy_Diverging_11_.add(color(244, 165, 130));
  RdGy_Diverging_11_.add(color(253, 219, 199));
  RdGy_Diverging_11_.add(color(255, 255, 255));
  RdGy_Diverging_11_.add(color(224, 224, 224));
  RdGy_Diverging_11_.add(color(186, 186, 186));
  RdGy_Diverging_11_.add(color(135, 135, 135));
  RdGy_Diverging_11_.add(color(77, 77, 77));
  RdGy_Diverging_11_.add(color(26, 26, 26));
  return RdGy_Diverging_11_;
}


// color RdGy_Diverging_10
public ArrayList get_RdGy_Diverging_10() {
  ArrayList RdGy_Diverging_10_ = new ArrayList();
  RdGy_Diverging_10_.add(color(103, 0, 31));
  RdGy_Diverging_10_.add(color(178, 24, 43));
  RdGy_Diverging_10_.add(color(214, 96, 77));
  RdGy_Diverging_10_.add(color(244, 165, 130));
  RdGy_Diverging_10_.add(color(253, 219, 199));
  RdGy_Diverging_10_.add(color(224, 224, 224));
  RdGy_Diverging_10_.add(color(186, 186, 186));
  RdGy_Diverging_10_.add(color(135, 135, 135));
  RdGy_Diverging_10_.add(color(77, 77, 77));
  RdGy_Diverging_10_.add(color(26, 26, 26));
  return RdGy_Diverging_10_;
}


// color BuGn_Sequential_6
public ArrayList get_BuGn_Sequential_6() {
  ArrayList BuGn_Sequential_6_ = new ArrayList();
  BuGn_Sequential_6_.add(color(237, 248, 251));
  BuGn_Sequential_6_.add(color(204, 236, 230));
  BuGn_Sequential_6_.add(color(153, 216, 201));
  BuGn_Sequential_6_.add(color(102, 194, 164));
  BuGn_Sequential_6_.add(color(44, 162, 95));
  BuGn_Sequential_6_.add(color(0, 109, 44));
  return BuGn_Sequential_6_;
}


// color BuGn_Sequential_7
public ArrayList get_BuGn_Sequential_7() {
  ArrayList BuGn_Sequential_7_ = new ArrayList();
  BuGn_Sequential_7_.add(color(237, 248, 251));
  BuGn_Sequential_7_.add(color(204, 236, 230));
  BuGn_Sequential_7_.add(color(153, 216, 201));
  BuGn_Sequential_7_.add(color(102, 194, 164));
  BuGn_Sequential_7_.add(color(65, 174, 118));
  BuGn_Sequential_7_.add(color(35, 139, 69));
  BuGn_Sequential_7_.add(color(0, 88, 36));
  return BuGn_Sequential_7_;
}


// color BuGn_Sequential_4
public ArrayList get_BuGn_Sequential_4() {
  ArrayList BuGn_Sequential_4_ = new ArrayList();
  BuGn_Sequential_4_.add(color(237, 248, 251));
  BuGn_Sequential_4_.add(color(178, 226, 226));
  BuGn_Sequential_4_.add(color(102, 194, 164));
  BuGn_Sequential_4_.add(color(35, 139, 69));
  return BuGn_Sequential_4_;
}


// color BuGn_Sequential_5
public ArrayList get_BuGn_Sequential_5() {
  ArrayList BuGn_Sequential_5_ = new ArrayList();
  BuGn_Sequential_5_.add(color(237, 248, 251));
  BuGn_Sequential_5_.add(color(178, 226, 226));
  BuGn_Sequential_5_.add(color(102, 194, 164));
  BuGn_Sequential_5_.add(color(44, 162, 95));
  BuGn_Sequential_5_.add(color(0, 109, 44));
  return BuGn_Sequential_5_;
}


// color BuGn_Sequential_3
public ArrayList get_BuGn_Sequential_3() {
  ArrayList BuGn_Sequential_3_ = new ArrayList();
  BuGn_Sequential_3_.add(color(229, 245, 249));
  BuGn_Sequential_3_.add(color(153, 216, 201));
  BuGn_Sequential_3_.add(color(44, 162, 95));
  return BuGn_Sequential_3_;
}


// color BuGn_Sequential_8
public ArrayList get_BuGn_Sequential_8() {
  ArrayList BuGn_Sequential_8_ = new ArrayList();
  BuGn_Sequential_8_.add(color(247, 252, 253));
  BuGn_Sequential_8_.add(color(229, 245, 249));
  BuGn_Sequential_8_.add(color(204, 236, 230));
  BuGn_Sequential_8_.add(color(153, 216, 201));
  BuGn_Sequential_8_.add(color(102, 194, 164));
  BuGn_Sequential_8_.add(color(65, 174, 118));
  BuGn_Sequential_8_.add(color(35, 139, 69));
  BuGn_Sequential_8_.add(color(0, 88, 36));
  return BuGn_Sequential_8_;
}


// color BuGn_Sequential_9
public ArrayList get_BuGn_Sequential_9() {
  ArrayList BuGn_Sequential_9_ = new ArrayList();
  BuGn_Sequential_9_.add(color(247, 252, 253));
  BuGn_Sequential_9_.add(color(229, 245, 249));
  BuGn_Sequential_9_.add(color(204, 236, 230));
  BuGn_Sequential_9_.add(color(153, 216, 201));
  BuGn_Sequential_9_.add(color(102, 194, 164));
  BuGn_Sequential_9_.add(color(65, 174, 118));
  BuGn_Sequential_9_.add(color(35, 139, 69));
  BuGn_Sequential_9_.add(color(0, 109, 44));
  BuGn_Sequential_9_.add(color(0, 68, 27));
  return BuGn_Sequential_9_;
}


// color PuRd_Sequential_7
public ArrayList get_PuRd_Sequential_7() {
  ArrayList PuRd_Sequential_7_ = new ArrayList();
  PuRd_Sequential_7_.add(color(241, 238, 246));
  PuRd_Sequential_7_.add(color(212, 185, 218));
  PuRd_Sequential_7_.add(color(201, 148, 199));
  PuRd_Sequential_7_.add(color(223, 101, 176));
  PuRd_Sequential_7_.add(color(231, 41, 138));
  PuRd_Sequential_7_.add(color(206, 18, 86));
  PuRd_Sequential_7_.add(color(145, 0, 63));
  return PuRd_Sequential_7_;
}


// color PuRd_Sequential_6
public ArrayList get_PuRd_Sequential_6() {
  ArrayList PuRd_Sequential_6_ = new ArrayList();
  PuRd_Sequential_6_.add(color(241, 238, 246));
  PuRd_Sequential_6_.add(color(212, 185, 218));
  PuRd_Sequential_6_.add(color(201, 148, 199));
  PuRd_Sequential_6_.add(color(223, 101, 176));
  PuRd_Sequential_6_.add(color(221, 28, 119));
  PuRd_Sequential_6_.add(color(152, 0, 67));
  return PuRd_Sequential_6_;
}


// color PuRd_Sequential_5
public ArrayList get_PuRd_Sequential_5() {
  ArrayList PuRd_Sequential_5_ = new ArrayList();
  PuRd_Sequential_5_.add(color(241, 238, 246));
  PuRd_Sequential_5_.add(color(215, 181, 216));
  PuRd_Sequential_5_.add(color(223, 101, 176));
  PuRd_Sequential_5_.add(color(221, 28, 119));
  PuRd_Sequential_5_.add(color(152, 0, 67));
  return PuRd_Sequential_5_;
}


// color PuRd_Sequential_4
public ArrayList get_PuRd_Sequential_4() {
  ArrayList PuRd_Sequential_4_ = new ArrayList();
  PuRd_Sequential_4_.add(color(241, 238, 246));
  PuRd_Sequential_4_.add(color(215, 181, 216));
  PuRd_Sequential_4_.add(color(223, 101, 176));
  PuRd_Sequential_4_.add(color(206, 18, 86));
  return PuRd_Sequential_4_;
}


// color PuRd_Sequential_3
public ArrayList get_PuRd_Sequential_3() {
  ArrayList PuRd_Sequential_3_ = new ArrayList();
  PuRd_Sequential_3_.add(color(231, 225, 239));
  PuRd_Sequential_3_.add(color(201, 148, 199));
  PuRd_Sequential_3_.add(color(221, 28, 119));
  return PuRd_Sequential_3_;
}


// color GnBu_Sequential_8
public ArrayList get_GnBu_Sequential_8() {
  ArrayList GnBu_Sequential_8_ = new ArrayList();
  GnBu_Sequential_8_.add(color(247, 252, 240));
  GnBu_Sequential_8_.add(color(224, 243, 219));
  GnBu_Sequential_8_.add(color(204, 235, 197));
  GnBu_Sequential_8_.add(color(168, 221, 181));
  GnBu_Sequential_8_.add(color(123, 204, 196));
  GnBu_Sequential_8_.add(color(78, 179, 211));
  GnBu_Sequential_8_.add(color(43, 140, 190));
  GnBu_Sequential_8_.add(color(8, 88, 158));
  return GnBu_Sequential_8_;
}


// color GnBu_Sequential_9
public ArrayList get_GnBu_Sequential_9() {
  ArrayList GnBu_Sequential_9_ = new ArrayList();
  GnBu_Sequential_9_.add(color(247, 252, 240));
  GnBu_Sequential_9_.add(color(224, 243, 219));
  GnBu_Sequential_9_.add(color(204, 235, 197));
  GnBu_Sequential_9_.add(color(168, 221, 181));
  GnBu_Sequential_9_.add(color(123, 204, 196));
  GnBu_Sequential_9_.add(color(78, 179, 211));
  GnBu_Sequential_9_.add(color(43, 140, 190));
  GnBu_Sequential_9_.add(color(8, 104, 172));
  GnBu_Sequential_9_.add(color(8, 64, 129));
  return GnBu_Sequential_9_;
}


// color GnBu_Sequential_6
public ArrayList get_GnBu_Sequential_6() {
  ArrayList GnBu_Sequential_6_ = new ArrayList();
  GnBu_Sequential_6_.add(color(240, 249, 232));
  GnBu_Sequential_6_.add(color(204, 235, 197));
  GnBu_Sequential_6_.add(color(168, 221, 181));
  GnBu_Sequential_6_.add(color(123, 204, 196));
  GnBu_Sequential_6_.add(color(67, 162, 202));
  GnBu_Sequential_6_.add(color(8, 104, 172));
  return GnBu_Sequential_6_;
}


// color GnBu_Sequential_7
public ArrayList get_GnBu_Sequential_7() {
  ArrayList GnBu_Sequential_7_ = new ArrayList();
  GnBu_Sequential_7_.add(color(240, 249, 232));
  GnBu_Sequential_7_.add(color(204, 235, 197));
  GnBu_Sequential_7_.add(color(168, 221, 181));
  GnBu_Sequential_7_.add(color(123, 204, 196));
  GnBu_Sequential_7_.add(color(78, 179, 211));
  GnBu_Sequential_7_.add(color(43, 140, 190));
  GnBu_Sequential_7_.add(color(8, 88, 158));
  return GnBu_Sequential_7_;
}


// color GnBu_Sequential_4
public ArrayList get_GnBu_Sequential_4() {
  ArrayList GnBu_Sequential_4_ = new ArrayList();
  GnBu_Sequential_4_.add(color(240, 249, 232));
  GnBu_Sequential_4_.add(color(186, 228, 188));
  GnBu_Sequential_4_.add(color(123, 204, 196));
  GnBu_Sequential_4_.add(color(43, 140, 190));
  return GnBu_Sequential_4_;
}


// color GnBu_Sequential_5
public ArrayList get_GnBu_Sequential_5() {
  ArrayList GnBu_Sequential_5_ = new ArrayList();
  GnBu_Sequential_5_.add(color(240, 249, 232));
  GnBu_Sequential_5_.add(color(186, 228, 188));
  GnBu_Sequential_5_.add(color(123, 204, 196));
  GnBu_Sequential_5_.add(color(67, 162, 202));
  GnBu_Sequential_5_.add(color(8, 104, 172));
  return GnBu_Sequential_5_;
}


// color GnBu_Sequential_3
public ArrayList get_GnBu_Sequential_3() {
  ArrayList GnBu_Sequential_3_ = new ArrayList();
  GnBu_Sequential_3_.add(color(224, 243, 219));
  GnBu_Sequential_3_.add(color(168, 221, 181));
  GnBu_Sequential_3_.add(color(67, 162, 202));
  return GnBu_Sequential_3_;
}


// color PuRd_Sequential_9
public ArrayList get_PuRd_Sequential_9() {
  ArrayList PuRd_Sequential_9_ = new ArrayList();
  PuRd_Sequential_9_.add(color(247, 244, 249));
  PuRd_Sequential_9_.add(color(231, 225, 239));
  PuRd_Sequential_9_.add(color(212, 185, 218));
  PuRd_Sequential_9_.add(color(201, 148, 199));
  PuRd_Sequential_9_.add(color(223, 101, 176));
  PuRd_Sequential_9_.add(color(231, 41, 138));
  PuRd_Sequential_9_.add(color(206, 18, 86));
  PuRd_Sequential_9_.add(color(152, 0, 67));
  PuRd_Sequential_9_.add(color(103, 0, 31));
  return PuRd_Sequential_9_;
}


// color PuRd_Sequential_8
public ArrayList get_PuRd_Sequential_8() {
  ArrayList PuRd_Sequential_8_ = new ArrayList();
  PuRd_Sequential_8_.add(color(247, 244, 249));
  PuRd_Sequential_8_.add(color(231, 225, 239));
  PuRd_Sequential_8_.add(color(212, 185, 218));
  PuRd_Sequential_8_.add(color(201, 148, 199));
  PuRd_Sequential_8_.add(color(223, 101, 176));
  PuRd_Sequential_8_.add(color(231, 41, 138));
  PuRd_Sequential_8_.add(color(206, 18, 86));
  PuRd_Sequential_8_.add(color(145, 0, 63));
  return PuRd_Sequential_8_;
}



// 
// LICENSE
// Copyright (c) 2002 Cynthia Brewer, Mark Harrower, and The Pennsylvania State University.
// Licensed under the Apache License, Version 2.0 (the ""License""); you may not use this file except in compliance with the License.
// You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
// This product includes color specifications and designs developed by Cynthia Brewer (http://colorbrewer.org/)
// 

}
class Genome{
	
	ChromIdeogram ideogram;

	ArrayList<BedAnnot> bed_annots = null;

	ArrayList<BedPEAnnot> bedpe_annots = null;

	String name;

	Genome(String chr_table, String n){

		ideogram = new ChromIdeogram(chr_table);
		name = n;

	} 

	public void addBed(String filename, int c, String glyph, int alpha_val){

		BedAnnot b = new BedAnnot(filename, glyph, c, alpha_val);

		if (bed_annots == null){
			bed_annots = new ArrayList<BedAnnot>();
		}
		
		bed_annots.add(b);
	

	}

	public void addBedPE(String filename, int c, int alpha_val){

		BedPEAnnot bpe = new BedPEAnnot(filename, c, alpha_val);

		if (bedpe_annots == null){
			bedpe_annots = new ArrayList<BedPEAnnot>();
		}
		
		bedpe_annots.add(bpe);
		
	}

	public void draw(float radius, float center_x, float center_y, float chr_width){
		ideogram.draw(radius, center_x, center_y, chr_width);
		// println("drew ideo");
		//to do: calculate fraction of radius as a function of number of bed annots
		if (bed_annots != null){
			for (BedAnnot b : bed_annots){
				// println("drew bed");
				b.draw(radius * 0.8f, center_x, center_y);
				// println("drew bed");
			}
		}

		if (bedpe_annots != null){
			for (BedPEAnnot bpe: bedpe_annots){
				// println("drawbedpe");
				bpe.draw(radius, center_x, center_y, chr_width);
				// println("drew bedpe");
			}
		}

		fill(50);
		text(name, center_x, center_y);
	}

	public float genToPolar(String chr_name, int pos){
		return ideogram.genToPolar(chr_name, pos);
	}


}
//methods for drawing shapes given polar coordinates

public void intBand(float start_angle, float end_angle, float radius, float center_x, float center_y, float band_width, int col, int alpha_val){

	//if angle is too small, make it the minimum to be seen on screen
	if (end_angle - start_angle < 0.002f){
			start_angle -= 0.001f;
			end_angle += 0.001f;
	}

	pushMatrix();
	translate(center_x, center_y);

	float outside_r = radius + band_width / 2;
	float inside_r = radius - band_width / 2; 

	float middle_angle = start_angle + ((end_angle - start_angle) / 2);

	float int_control_angle = start_angle - ((end_angle - start_angle) / 2);
	float ext_control_angle = end_angle + ((end_angle - start_angle) / 2);
	// println(middle_angle);

	//exterior control point 1
	float cp1_x = outside_r * cos(int_control_angle);
	float cp1_y = outside_r * sin(int_control_angle);

	//interior control point 2
	float cp2_x = inside_r * cos(int_control_angle);
	float cp2_y = inside_r * sin(int_control_angle);

	//interior control point 3
	float cp3_x = inside_r * cos(ext_control_angle);
	float cp3_y = inside_r * sin(ext_control_angle);

	//exterior control point 4
	float cp4_x = outside_r * cos(ext_control_angle);
	float cp4_y = outside_r * sin(ext_control_angle);


	//point 1
	float a = outside_r * cos(start_angle);
	float b = outside_r * sin(start_angle);

	//point 2
	float c = inside_r * cos(start_angle);
	float d = inside_r * sin(start_angle);

	//point 3
	float e = inside_r * cos(middle_angle);
	float f = inside_r * sin(middle_angle);

	//point 4
	float g = inside_r * cos(end_angle);
	float h = inside_r * sin(end_angle);

	//point 5
	float i = outside_r * cos(end_angle);
	float j = outside_r * sin(end_angle);

	//point 6
	float k = outside_r * cos(middle_angle);
	float l = outside_r * sin(middle_angle);

	// println(a, b, c, d, e, f, g, h, i, j, k, l);

	noStroke();
	fill(col, alpha_val);
	beginShape();
		vertex(a,b);
		vertex(c, d);
		curveVertex(cp2_x, cp2_y);
		curveVertex(c,d);
		curveVertex(e,f);
		curveVertex(g,h);
		curveVertex(cp3_x, cp3_y);
		vertex(g,h);
		vertex(i,j);
		curveVertex(cp4_x, cp4_y);
		curveVertex(i,j);
		curveVertex(k,l);
		curveVertex(a,b);
		curveVertex(cp1_x, cp1_y);
	endShape();

	popMatrix();



}

public void intPairBezier(float start_angle1, float end_angle1, float start_angle2, float end_angle2, float radius, float center_x, float center_y, int col, int alpha_val){



	//first interval
	//point 1
	float a = radius * cos(start_angle1);
	float b = radius * sin(start_angle1);

	//point 2
	float c = radius * cos(end_angle1);
	float d = radius * sin(end_angle1);


	////////////////

	float int_control_angle1 = start_angle1 - ((end_angle1 - start_angle1) / 2);
	float ext_control_angle1 = end_angle1 + ((end_angle1 - start_angle1) / 2);

	float middle_angle1 = start_angle1 + ((end_angle1 - start_angle1) / 2);

	// println(middle_angle1);

	//control point 1
	float cp1_x = radius * cos(int_control_angle1);
	float cp1_y = radius * sin(int_control_angle1);

	//control point 2
	float cp2_x = radius * cos(ext_control_angle1);
	float cp2_y = radius * sin(ext_control_angle1);

	//middle point 1
	float m1_x = radius * cos(middle_angle1);
	float m1_y = radius * sin(middle_angle1);
	///////////////


	//second interval
	//point 3
	float e = radius * cos(start_angle2);
	float f = radius * sin(start_angle2);

	//point 4
	float g = radius * cos(end_angle2);
	float h = radius * sin(end_angle2);

	////////////////

	float int_control_angle2 = start_angle2 - ((end_angle2 - start_angle2) / 2);
	float ext_control_angle2 = end_angle2 + ((end_angle2 - start_angle2) / 2);

	float middle_angle2 = start_angle2 + ((end_angle2 - start_angle2) / 2);

	// println(middle_angle2);

	//control point 3
	float cp3_x = radius * cos(int_control_angle2);
	float cp3_y = radius * sin(int_control_angle2);

	//control point 4
	float cp4_x = radius * cos(ext_control_angle2);
	float cp4_y = radius * sin(ext_control_angle2);

	//middle point 2
	float m2_x = radius * cos(middle_angle2);
	float m2_y = radius * sin(middle_angle2);
	///////////////

	pushMatrix();
	translate(center_x, center_y);


	fill(col, alpha_val);
  	noStroke();
	beginShape();
		vertex(a,b);
		curveVertex(cp1_x, cp1_y);
		curveVertex(a,b);
		curveVertex(m1_x, m1_y);
		curveVertex(c,d);
		curveVertex(cp2_x, cp2_y);
		vertex(c,d);
		// bezierVertex(e/10, d/10, e,f, e,f);
		// vertex(g, h);
		// //push the control point in the opposite side of the curve to make it thicker
		// bezierVertex(0-g/10, 0-b/10, a, b, a, b);

		bezierVertex(0, 0, e,f, e,f);
		curveVertex(cp3_x, cp3_y);
		curveVertex(e, f);
		curveVertex(m2_x, m2_y);
		curveVertex(g, h);
		curveVertex(cp4_x, cp4_y);
		vertex(g,h);
		//push the control point in the opposite side of the curve to make it thicker
		bezierVertex(0, 0, a, b, a, b);

	endShape();

	popMatrix();

}

public void intMidDot(float start_angle, float end_angle, float radius, float center_x, float center_y, int c, int alpha_val){

	pushMatrix();
	translate(center_x, center_y);

	float middle_angle = start_angle + (end_angle - start_angle)/2;

	//point 1
	float a = x_polToCart(middle_angle,radius);
	float b = y_polToCart(middle_angle,radius);



	fill(c, alpha_val);
	ellipse(a, b, 10, 10);


	popMatrix();

}


public float x_polToCart(float angle, float radius){

	return radius * cos(angle);
}

public float y_polToCart(float angle, float radius){
	return radius * sin(angle);
}








  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "ChromoViz" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
