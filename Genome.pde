class Genome{
	
	ChromIdeogram ideogram;

	ArrayList<BedAnnot> bed_annots;

	ArrayList<BedPEAnnot> bedpe_annots;

	int chr_width = 40;

	Genome(String chr_table){

		ideogram = new ChromIdeogram(chr_table, chr_width);


	} 

	void addBed(String filename, color c, String glyph, int alpha_val){

		BedAnnot b = new BedAnnot(filename, glyph, c, alpha_val);

		if (bed_annots == null){
			bed_annots = new ArrayList<BedAnnot>();
		}
		
		bed_annots.add(b);
	

	}

	void addBedPE(String filename, color c, int alpha_val){

		BedPEAnnot bpe = new BedPEAnnot(filename, c, alpha_val, chr_width);

		if (bedpe_annots == null){
			bedpe_annots = new ArrayList<BedPEAnnot>();
		}
		
		bedpe_annots.add(bpe);
		
	}

	void draw(float radius, float center_x, float center_y){
		ideogram.draw(radius, center_x, center_y);
		//to do: calculate fraction of radius as a function of number of bed annots
		for (BedAnnot b : bed_annots){
			b.draw(radius * 0.8, center_x, center_y);
		}

		for (BedPEAnnot bpe: bedpe_annots){
			bpe.draw(radius, center_x, center_y);
		}
	}

	float genToPolar(String chr_name, int pos){
		return ideogram.genToPolar(chr_name, pos);
	}


}