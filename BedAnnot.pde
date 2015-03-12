//class to store a bed file (bed3)
//methods to draw it in different ways


class BedAnnot{
	
	Table bed_table;

	BedAnnot(String bed_file){

		bed_table = loadTable(bed_file, "header, tsv");
	}


	//draw as colored interval
	void drawAsInt(float radius){

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

			start_angle = chr_ideogram.genToPolar(chr_name, row.getInt("start"));
			end_angle = chr_ideogram.genToPolar(chr_name, row.getInt("end"));
			intBand(start_angle, end_angle, radius, width/2, height/2, 40, 180);
		}

	}

	void drawAsDot(float radius){

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

			start_angle = chr_ideogram.genToPolar(chr_name, row.getInt("start"));
			end_angle = chr_ideogram.genToPolar(chr_name, row.getInt("end"));
			intMidDot(start_angle, end_angle, radius, width/2, height/2);
		}

	}

	//draw as dot

	//draw as triangle

	//draw as scatter?
}