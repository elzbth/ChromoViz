float[] angles;

ChromIdeogram chr_ideogram;

BedAnnot bed_table;

int full_radius;

float spacer_rad = 0.02;

int num_chr;

float zoom  = 1; 

void setup(){

	chr_ideogram = new ChromIdeogram("hg19.seqs.chr1-22.X.Y.fa.tsv");

	bed_table = new BedAnnot("test.bed");


	size(800,800);
	// noLoop();

}

void draw(){
	background(255);
	full_radius = int(min(width, height) * 0.4);
	
	scale(zoom);

  	chr_ideogram.draw(full_radius);

  	bed_table.drawAsInt(full_radius * 0.8);
  	

  // println(mouseX, mouseY);
}



// void drawBedTable(float radius, Table bedtable){
// 	float start_angle = 0.0; 
// 	float end_angle = 0.0; 
// 	String chr_name = "";
// 	int startpos = 0;
// 	int endpos = 0;

// 	for (TableRow row : bedtable.rows()){

// 		chr_name = row.getString("chr");
// 		// startpos = row.getInt("start");
// 		// endpos = row.getInt("end");

// 		// println(chr_name, startpos, endpos);

// 		start_angle = chr_ideogram.genToPolar(chr_name, row.getInt("start"));
// 		end_angle = chr_ideogram.genToPolar(chr_name, row.getInt("end"));
// 		intBand(start_angle, end_angle, radius, width/2, height/2, 40, 180);

// 	}

// }



void intBand(float start_angle, float end_angle, float radius, float center_x, float center_y, float band_width, float grey_val){

	//if angle is too small, make it the minimum to be seen on screen
	if (end_angle - start_angle < 0.002){
			start_angle -= 0.001;
			end_angle += 0.001;
	}

	pushMatrix();
	translate(center_x, center_y);

	float outside_r = radius + band_width / 2;
	float inside_r = radius - band_width / 2; 

	float middle_angle = start_angle + ((end_angle - start_angle) / 2);

	float int_control_angle = start_angle - ((end_angle - start_angle) / 2);
	float ext_control_angle = end_angle + ((end_angle - start_angle) / 2);
	// println(middle_angle);

	//interior control point 1
	float cp1_x = outside_r * cos(int_control_angle);
	float cp1_y = outside_r * sin(int_control_angle);

	//interior control point 2
	float cp2_x = inside_r * cos(int_control_angle);
	float cp2_y = inside_r * sin(int_control_angle);

	//exterior control point 3
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
	fill(grey_val);
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


// Table loadBed(String bedfile){
// 	// println(bedfile);
// 	Table table = loadTable(bedfile, "header, tsv");
// 	return table;
// }





// ------ key and mouse events ------

void keyPressed(){
  

    if (keyCode == UP) zoom += 0.05;
    if (keyCode == DOWN) zoom -= 0.05;
    zoom = max(zoom, 0.1);

    
}







