class BedPEAnnot{
	
	Table bed_pe_table;

	BedPEAnnot(String bed_file){

		bed_pe_table = loadTable(bed_file, "header, tsv");
	}

	void drawAsIntPairBezier(float radius){

		float start_angle1 = 0.0; 
		float end_angle1 = 0.0; 
		String chr_name1 = "";

		float start_angle2 = 0.0; 
		float end_angle2 = 0.0; 
		String chr_name2 = "";



		for (TableRow row : bed_pe_table.rows()){

			chr_name1 = row.getString("chr1");
			start_angle1 = chr_ideogram.genToPolar(chr_name1, row.getInt("start1"));
			end_angle1 = chr_ideogram.genToPolar(chr_name1, row.getInt("end1"));

			chr_name2 = row.getString("chr2");
			start_angle2 = chr_ideogram.genToPolar(chr_name2, row.getInt("start2"));
			end_angle2 = chr_ideogram.genToPolar(chr_name2, row.getInt("end2"));


			intPairBezier(start_angle1, end_angle1, start_angle2, end_angle2, radius, width/2, height/2);

		}

	}
}