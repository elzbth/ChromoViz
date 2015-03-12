//methods for drawing shapes given polar coordinates

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

void intPairBezier(float start_angle1, float end_angle1, float start_angle2, float end_angle2, float radius, float center_x, float center_y){

	pushMatrix();
	translate(center_x, center_y);

	//first interval
	//point 1
	float a = radius * cos(start_angle1);
	float b = radius * sin(start_angle1);

	//point 2
	float c = radius * cos(end_angle1);
	float d = radius * sin(end_angle1);

	//second interval
	//point 3
	float e = radius * cos(start_angle2);
	float f = radius * sin(start_angle2);

	//point 4
	float g = radius * cos(end_angle2);
	float h = radius * sin(end_angle2);



	fill(102,194,165, 180);
  	noStroke();
	beginShape();
		vertex(a,b);
		vertex(c,d);
		// bezierVertex(e/10, d/10, e,f, e,f);
		// vertex(g, h);
		// //push the control point in the opposite side of the curve to make it thicker
		// bezierVertex(0-g/10, 0-b/10, a, b, a, b);

		bezierVertex(0, 0, e,f, e,f);
		vertex(g, h);
		//push the control point in the opposite side of the curve to make it thicker
		bezierVertex(0, 0, a, b, a, b);

	endShape();

	popMatrix();

}

void intMidDot(float start_angle, float end_angle, float radius, float center_x, float center_y){

	pushMatrix();
	translate(center_x, center_y);

	float middle_angle = start_angle + (end_angle - start_angle)/2;

	//point 1
	float a = x_polToCart(middle_angle,radius);
	float b = y_polToCart(middle_angle,radius);



	fill(252,141,98);
	ellipse(a, b, 10, 10);


	popMatrix();

}


// void intMidDot(float start_angle, float end_angle, float radius, float center_x, float center_y){

// 	pushMatrix();
// 	translate(center_x, center_y);

// 	middle_angle = start_angle + (end_angle - start_angle)/2;

// 	//point 1
// 	float a = x_polToCart(start_angle,radius);
// 	float b = y_polToCart(start_angle,radius);

// 	//point 2
// 	float c = x_polToCart(end_angle, radius);
// 	float d = y_polToCart(end_angle,radius);

// 	fill(252,141,98);
// 	ellipse(x, y, width, height);


// 	popMatrix();

// }

float x_polToCart(float angle, float radius){

	return radius * cos(angle);
}

float y_polToCart(float angle, float radius){
	return radius * sin(angle);
}








