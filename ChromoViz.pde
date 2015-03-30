import processing.opengl.*;
import peasy.*;
import processing.pdf.*;


//for nice image panning, rotation and zoom
PeasyCam cam;

//for nice colors
ColorBrewer brewer = new ColorBrewer();

// float[] angles;



//fraction of window the full radius occupies 
int full_radius;

//radians to space chromosomes
float spacer_rad = 0.02;

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

boolean record_pdf = false;


void setup(){

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

void draw(){

	if (record_pdf){
		beginRecord(PDF, "frame-####.pdf");
	}

	background(255);
	full_radius = int(min(width, height) * 0.4);


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

		int radius = int(( width / (max(numcols, numrows) + 1) ) * 0.4);

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

	if (record_pdf){
		endRecord();
		record_pdf = false;
	}

	// scale(zoom);  

}

void parseConfigFile(File selection) {
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

color parseColor(String s){
	String[] rgb = split(s, ",");
	int r = Integer.parseInt(rgb[0]);
	int g = Integer.parseInt(rgb[1]);
	int b = Integer.parseInt(rgb[2]);

	return color(r,g,b);

}
// }

void mouseEntered(MouseEvent e) {
  loop();
}

void mouseExited(MouseEvent e) {
  noLoop();
}

void keyPressed() {
  if (key == 'o') {
    zoom += 0.1;
  } else if (key == 'i'){
    zoom -= 0.1;
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
  else if (key == 'w'){
  	record_pdf = true;
  }

}









