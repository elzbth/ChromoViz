//class to store a bed file (bed3)
//methods to draw it in different ways


class BedAnnot{
	
	Table bed_table;
	String glyph;
	color col;
	int alpha_val;

	BedAnnot(String bed_file, String g, color c, int a){

		bed_table = loadTable(bed_file, "header, tsv");
		glyph = g;
		alpha_val = a;
		col = c;
		println("tbale", bed_file, bed_table);

	}

	void draw(float radius, float x, float y){
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
	void drawAsInt(float radius, float x, float y){

		float start_angle = 0.0; 
		float end_angle = 0.0; 
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
	void drawAsDot(float radius, float x, float y){

		float start_angle = 0.0; 
		float end_angle = 0.0; 
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