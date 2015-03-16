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

int shift = 300;



void setup(){

  size(800,800, P3D);

	cam = new PeasyCam(this, width/2, height/2, 0, 600);
	// cam.setMinimumDistance(50);
  	// cam.setMaximumDistance(500);

	Genome genome1 = new Genome("hg19.seqs.chr1-22.X.Y.fa.tsv");

	genome1.addBed("test.bed", "dot", color(215,25,28), 150);

	genome1.addBedPE("test.bedpe", color(166,217,106), 150);

  genome = genome1;

  Genome genome2 = new Genome("hg19.seqs.chr1-22.X.Y.fa.tsv");

  genome2.addBed("test.bed", "dot", color(215,25,28), 150);

  genome2.addBedPE("test2.bedpe", color(166,217,106), 150);


  genomes.add(genome1);
  genomes.add(genome2);

  println(genomes);

	
	frameRate(30);
	// noLoop();

}

void draw(){


  background(255);
  full_radius = int(min(width, height) * 0.4);


  int counter = 0;



  pushMatrix();
  for (Genome genome : genomes){

    translate(0, 0, counter * shift);
    genome.draw(full_radius, width/2, width/2);
    counter ++;

  }
  // println(counter);
  popMatrix();

}







// ------ key and mouse events ------

// void keyPressed(){


    
// }

// void mousePressed() {

// 	// canvas dragging


// 	println("click!");
// 	mouseClick = new PVector(mouseX, mouseY);


// }

// void mouseDragged(){

// 	if(mouseButton==LEFT){
// 	  	PVector mousePos = new PVector(mouseX, mouseY);
// 	    targetOffset = PVector.sub(mousePos, mouseClick);
// 	}
// 	if(mouseButton==RIGHT){
// 		x_angle = map(mouseX-mouseClick.x, 0, width, 0, TWO_PI);
// 		y_angle = map(mouseY-mouseClick.y, 0, width, 0, TWO_PI);
// 	}

// }


// void mouseReleased() {

// 	if (dragging){
// 		dragging = false;
// 	}

// 	if (rotating){
// 		rotating = false;
// 		rotateXangle += x_angle;
// 		rotateYangle += y_angle;
// 	}


// }

void mouseEntered(MouseEvent e) {
  loop();
}

void mouseExited(MouseEvent e) {
  noLoop();
}







