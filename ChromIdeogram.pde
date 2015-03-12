class ChromIdeogram{


	Table chr_table;
	float tot_length;
	

	ChromIdeogram(String table_name){

		chr_table = loadTable(table_name, "header");

		num_chr = chr_table.getRowCount();

		//take into account the spacers between chromosomes 
		float tot_available_angle = TWO_PI - (num_chr*spacer_rad);

		angles = new float[chr_table.getRowCount()];



		//add columns to table to store info for polar coordinates

		//to store the start angle for polar coordinates of this chr
		chr_table.addColumn("start_angle", Table.FLOAT);
		//to store the end angle for polar coordinates of this chr
		chr_table.addColumn("end_angle", Table.FLOAT);
		//to store int value of chr length
		chr_table.addColumn("chr_length_bp", Table.INT);

		
		tot_length = 0.0;
		for (TableRow row : chr_table.rows()){
			tot_length += row.getInt("length");
		}

		float start_angle = 0;

		int index = 0;

		for (TableRow row : chr_table.rows()){
			
			int chr_length = row.getInt("length");
			String chr_name = row.getString("chr_name");
			
			float angle = map(chr_length, 0, tot_length, 0.0, tot_available_angle);

			row.setFloat("start_angle", start_angle);
			row.setFloat("end_angle", start_angle + angle);

			start_angle = start_angle + angle + spacer_rad;
			println(index, chr_name);
			index ++;

		}
	} 

	void draw(float radius){

		int i = 0;
		for (TableRow row : chr_table.rows()){
			int gray_val = int(map(i, 0, num_chr, 0, 255));
			intBand(row.getFloat("start_angle"), row.getFloat("end_angle"), radius, width/2, height/2, 40, gray_val);
			i++;
		}
	}

	TableRow get_chr_table_row(String chr_name){

		return chr_table.getRow(getChrIndex(chr_name));

	}

	int getChrIndex(String chr_name){
		if (chr_name.equals( "X")){
			return 22;
		}
		else if (chr_name.equals("Y")){
				return 23;
			
		} else{

			// println(chr_name, int(chr_name));
			return int(chr_name) - 1;
		}
	}

	float genToPolar(String chr_name, int pos){

		// println(chr, pos);
		TableRow chr_ref = chr_table.getRow(getChrIndex(chr_name));

		// find the angle corresponding to the chromosome and position, given the start and end angles defined for that chromosome
		float angle = map(pos, 0, chr_ref.getInt("length"), chr_ref.getFloat("start_angle"), chr_ref.getFloat("end_angle"));
		// println(angle);
		return angle;
	}
}