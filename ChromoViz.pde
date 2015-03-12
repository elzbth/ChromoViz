float[] angles;

ChromIdeogram chr_ideogram;

BedAnnot bed_table;

BedPEAnnot bed_pe_table;

int full_radius;

float spacer_rad = 0.02;

int num_chr;

float zoom  = 1; 

void setup(){

	chr_ideogram = new ChromIdeogram("hg19.seqs.chr1-22.X.Y.fa.tsv");

	bed_table = new BedAnnot("test.bed");

	bed_pe_table = new BedPEAnnot("test.bedpe");



	size(800,800);
	// noLoop();

}

void draw(){
	background(255);
	full_radius = int(min(width, height) * 0.4);
	
	scale(zoom);

  	

  	bed_table.drawAsInt(full_radius * 0.8);
  	bed_table.drawAsDot(full_radius * 0.9);

  	bed_pe_table.drawAsIntPairBezier(full_radius);

  	chr_ideogram.draw(full_radius);

  	
  	

  // println(mouseX, mouseY);
}







// ------ key and mouse events ------

void keyPressed(){
  

    if (keyCode == UP) zoom += 0.05;
    if (keyCode == DOWN) zoom -= 0.05;
    zoom = max(zoom, 0.1);

    
}







