import processing.opengl.*;
import peasy.*;

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

int annot_alpha_val = 150;


boolean wait = true;


void setup(){

  size(800,800, P3D);


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


  background(255);
  full_radius = int(min(width, height) * 0.4);


  int counter = 0;



  
  for (Genome genome : genomes){

  	pushMatrix();
    translate(0, 0, counter * shift);
    genome.draw(full_radius, width/2, width/2);
    counter ++;
    popMatrix();

  }
  // println(counter);
  

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
				println("add genome");
			}

			//bed line is of the form
			//bed	filename	r,g,b	[dot|interval]
			else if (tokens[0].equals("bed")){
				current_genome.addBed(tokens[1], parseColor(tokens[2]), tokens[3], annot_alpha_val);
				println("add bed");
			}

			//bedpe line is of the form
			//bedpe	filename	r,g,b
			else if (tokens[0].equals("bedpe")){
				current_genome.addBedPE(tokens[1], parseColor(tokens[2]), annot_alpha_val);
				println("add bedpe");
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







