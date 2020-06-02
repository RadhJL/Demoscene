#version 430
layout(local_size_x = 12, local_size_y = 1, local_size_z = 1) in ;

float infinity = -1. / 0.;
float biais = 0.1;
#define infinite_vec vec3(infinity);
#define infinite_seg segment(vec3(infinity), vec3(infinity));

layout(std430, binding = 0) buffer PixelBuffer {
  float data[];
}
Buffer;

layout(std430, binding = 1) buffer PixelBuffer1 {
  float data[];
}
Buffer1;

struct segment {
  vec3 a;
  vec3 b;
};

struct sphere {
  vec3 origin;
  float radius;
};

struct cylindre {
  vec3 center;
  float radius;
  float height;
};

struct cone {
  vec3 center;
  float radius;
  float height;
};

struct plan {
  vec3 normal;
  vec3 a;
  vec3 b;
  vec3 c;
  vec3 d;
};
struct object {
  int id;
  int indice;
};

int inf = -25;

uint unique_thread_id = (gl_WorkGroupID.x) * gl_NumWorkGroups.y * gl_NumWorkGroups.z + (gl_WorkGroupID.y) * gl_NumWorkGroups.z + gl_WorkGroupID.z;
uint N = gl_NumWorkGroups.x * gl_NumWorkGroups.y * gl_NumWorkGroups.z;
uint thread_id = gl_LocalInvocationID.x;

uint group_id_x = gl_WorkGroupID.x;
uint group_id_y = gl_WorkGroupID.y;
uint group_id_z = gl_WorkGroupID.z;

int x = int(group_id_x + inf);
int y = int(group_id_y + inf);
int z = int(group_id_z + inf);

shared segment segments[12];
shared segment inter_normal[12];
shared bool segment_inter[12];
shared sphere sphere_list[10];
shared plan plan_list[10];
shared cone cone_list[10];
shared cylindre cylindre_list[10];
shared object object_list[40];
shared int object_size;
shared bool flag;
shared int count;
shared vec3 result;
shared vec3 gradient;

void synchronize() {
		  barrier();
}

//Sphere
float Sphere_gradient(vec3 p, vec3 c, float r){
	return (p.x - c.x)*(p.x - c.x) + (p.y - c.y)*(p.y - c.y) + (p.z - c.z)*(p.z - c.z) - r*r;
}

segment Sphere_Inter(segment s, sphere obj) {
		  segment result = infinite_seg;
		
		  float radius = obj.radius;
		  vec3 center = obj.origin;
		
		  float radius2 = radius * radius;
		  vec3 orig = vec3(s.a);
		  vec3 dir = normalize(s.b - s.a);
		
		  float t0, t1;
		  vec3 L = center - orig;
		  float tca = dot(L, dir);
		  float d2 = dot(L, L) - tca * tca;
		
		  if (d2 > radius2) return result;
		
		  float thc = sqrt(radius2 - d2);
		  t0 = tca - thc;
		  t1 = tca + thc;
		
		  if (t0 >= 0 && t0 <= 1) {
		    result.a = vec3(orig + t0 * dir);
		    return result;
		  } else if (t1 >= 0 && t1 <= 1) {
		    result.a = vec3(orig + t1 * dir);
			float b_x0 = Sphere_gradient( vec3(vec3(result.a) + vec3(biais, 0, 0)), center, radius);
			float b_x1 = Sphere_gradient( vec3(vec3(result.a) - vec3(biais, 0, 0)), center, radius);
			float b_y0 = Sphere_gradient( vec3(vec3(result.a) + vec3(0, biais, 0)), center, radius);
			float b_y1 = Sphere_gradient( vec3(vec3(result.a) - vec3(0, biais, 0)), center, radius);
			float b_z0 = Sphere_gradient( vec3(vec3(result.a) + vec3(0, 0, biais)), center, radius);
			float b_z1 = Sphere_gradient( vec3(vec3(result.a) - vec3(0, 0, biais)), center, radius);
			result.b =  vec3(b_x0-b_x1,b_y0-b_y1,b_z0-b_z1) / float(2 * biais);
		    return result;
		  }
		
		  return result;
}
//Cone
float Cone_gradient(vec3 p, vec3 c, float tan){
	return (p.x * c.x) * (p.x * c.x) + (p.z* c.z) * (p.z * c.z) - tan * (p.y * c.y) * (p.y * c.y); 
}

segment Cone_Inter(segment s, cone obj) {
		 
		  segment result = infinite_seg;
		  float radius = obj.radius;
		  vec3 center = obj.center;
		  float height = obj.height;
		  float radius2 = radius * radius;
		  vec3 pos = vec3(s.a);
		  vec3 dir = normalize(s.b - s.a);
		
		  float A = pos.x - center.x;
		  float B = pos.z - center.z;
		  float D = height - pos.y + center.y;
		
		  float tan = (radius / height) * (radius / height);
		
		  float a = (dir.x * dir.x) + (dir.z * dir.z) - (tan * (dir.y * dir.y));
		  float b = (2 * A * dir.x) + (2 * B * dir.z) + (2 * tan * D * dir.y);
		  float c = (A * A) + (B * B) - (tan * (D * D));
		
		  float delta = b * b - 4 * (a * c);
		  if (delta < 0.0) return result;
		
		  float t1 = (-b - sqrt(delta)) / (2 * a);
		  float t2 = (-b + sqrt(delta)) / (2 * a);
		  float t;
		
		  if (t1 > t2) t = t2;
		  else t = t1;
			t = t1;

		   if (t > 1.5 || t < -1.3)
		    	return result;
		
		  float r = pos.y + t * dir.y;
		
		  if ((r >= center.y - 0.3f ) && (r <= center.y + height )) {
		    float b_x0 = Cone_gradient( vec3(vec3(result.a) + vec3(biais, 0, 0)), center, tan);
			float b_x1 = Cone_gradient( vec3(vec3(result.a) - vec3(biais, 0, 0)), center, tan);
			float b_y0 = Cone_gradient( vec3(vec3(result.a) + vec3(0, biais, 0)), center, tan);
			float b_y1 = Cone_gradient( vec3(vec3(result.a) - vec3(0, biais, 0)), center, tan);
			float b_z0 = Cone_gradient( vec3(vec3(result.a) + vec3(0, 0, biais)), center, tan);
			float b_z1 = Cone_gradient( vec3(vec3(result.a) - vec3(0, 0, biais)), center, tan);
			result.a = vec3(pos + t * dir);
			result.b =  vec3(b_x0-b_x1,b_y0-b_y1,b_z0-b_z1) / float(2 * biais);
		    return result;
		  }
		
		  return result;
	}

//Cylindre
float Cylindre_gradient(vec3 p, vec3 c, float r){
	return (p.x-c.x)*(p.x-c.x) + (p.z-c.z)*(p.z-c.z) - r*r; 
}

segment Cylindre_Inter(segment s, cylindre obj) {
		  segment result = infinite_seg;
		
		  float radius = obj.radius;
		  vec3 center = obj.center;
		  float height = obj.height;
		
		  float radius2 = radius * radius;
		
		  vec3 pos = vec3(s.a);
		  vec3 dir = normalize(s.b - s.a);
		
		  float a = (dir.x * dir.x) + (dir.z * dir.z);
		  float b = 2 * (dir.x * (pos.x - center.x) + dir.z * (pos.z - center.z));
		  float c = (pos.x - center.x) * (pos.x - center.x) + (pos.z - center.z) * (pos.z - center.z) - (radius * radius);
		
		  float delta = b * b - 4 * (a * c);
		  if (delta < 0.0) return result;
		
		  float t1 = (-b - sqrt(delta)) / (2 * a);
		  float t2 = (-b + sqrt(delta)) / (2 * a);
		  float t;
		
		  if (t1 > t2) t = t2;
		 	 else t = t1;
		  
		  if (t > 1 || t < -1)
		    return result;
		 
		  float r = pos.y + t * dir.y;
		  if ((r >= center.y) && (r <= center.y + height)) {
			float b_x0 = Cylindre_gradient( vec3(vec3(result.a) + vec3(biais, 0, 0)), center, radius);
			float b_x1 = Cylindre_gradient( vec3(vec3(result.a) - vec3(biais, 0, 0)), center, radius);
			float b_y0 = Cylindre_gradient( vec3(vec3(result.a) + vec3(0, biais, 0)), center, radius);
			float b_y1 = Cylindre_gradient( vec3(vec3(result.a) - vec3(0, biais, 0)), center, radius);
			float b_z0 = Cylindre_gradient( vec3(vec3(result.a) + vec3(0, 0, biais)), center, radius);
			float b_z1 = Cylindre_gradient( vec3(vec3(result.a) - vec3(0, 0, biais)), center, radius);
		    result.a = vec3(pos + t * dir);
			result.b =  vec3(b_x0-b_x1,b_y0-b_y1,b_z0-b_z1) / float(2 * biais);
			return result;
		  }
		
		  return result;
}

//Plane
float Plane_gradient(vec3 p, vec3 n){
	return p.x * n.x + p.y * n.y + p.z * n.y;
}

bool isInside(vec3 q, plan obj) {
		  vec3 n = obj.normal;
		  vec3 a = obj.a;
		  vec3 b = obj.b;
		  vec3 c = obj.c;
		  vec3 d = obj.d;
		
		  vec3 ua = b - a, ub = c - b, uc = d - c, ud = a - d;
		  vec3 va = q - a, vb = q - b, vc = q - c, vd = q - d;
		
		  if ((dot(cross(ua, va), n) > 0) && (dot(cross(ub, vb), n) > 0) && (dot(cross(uc, vc), n) > 0) && (dot(cross(ud, vd), n) > 0))
		    return true;
		  else
		    return false;
}

segment Plan_Inter(segment ray, plan obj) {
		  segment result = infinite_seg;
		  vec3 n = obj.normal;
		  vec3 a = obj.a;
		
		  vec3 pos = vec3(ray.a);
		  vec3 dir = normalize(ray.b - ray.a);
		
		  vec3 vdif = a - pos;
		  float vdotn = dot(dir, n);
		
		  if ( abs(vdotn) < 1.e-4) return result;
		  float t = dot(vdif, n) / vdotn;
		
		  if (t > 1 || t < -1) return result;
		  vec3 q = pos + dir * t;

		  if (!isInside(q, obj)) return result;
			float b_x0 = Plane_gradient( vec3(vec3(result.a) + vec3(biais, 0,0 )), n);
		  	float b_x1 = Plane_gradient( vec3(vec3(result.a) - vec3(biais, 0, 0)), n);
			float b_y0 = Plane_gradient( vec3(vec3(result.a) + vec3(0, biais, 0)), n);
			float b_y1 = Plane_gradient( vec3(vec3(result.a) - vec3(0, biais, 0)), n);
			float b_z0 = Plane_gradient( vec3(vec3(result.a) + vec3(0, 0, biais)), n);
			float b_z1 = Plane_gradient( vec3(vec3(result.a) - vec3(0, 0, biais)), n);
		  	result.a = q;
			result.b =  vec3(b_x0-b_x1,b_y0-b_y1,b_z0-b_z1) / float(2 * biais);
		  	return result;
}

segment Inter(object obj, segment seg) {
	switch (obj.id) {
		case 1:
		return Sphere_Inter(seg, sphere_list[obj.indice]);
		break;
		case 2:
		return Plan_Inter(seg, plan_list[obj.indice]);
		break;
		case 3:
		return Cylindre_Inter(seg, cylindre_list[obj.indice]);
		break;
		case 4:
		return Cone_Inter(seg, cone_list[obj.indice]);
		break;
		default:
		synchronize(); // nothing
	}
}

void FindSegments(float x, float y, float z) {

	vec3 p0 = vec3(x, y + 1, z);
	vec3 p1 = vec3(x + 1, y + 1, z);
	vec3 p2 = vec3(x + 1, y + 1, z + 1);
	vec3 p3 = vec3(x, y + 1, z + 1);
	vec3 p4 = vec3(x, y, z);
	vec3 p5 = vec3(x + 1, y, z);
	vec3 p6 = vec3(x + 1, y, z + 1);
	vec3 p7 = vec3(x, y, z + 1);
	vec3 p[8] = {
	p0,
	p1,
	p2,
	p3,
	p4,
	p5,
	p6,
	p7
	};

	int j = 0;
	for (int i = 0; i <= 7; i++) {
	if (i <= 3)
		segments[j++] = segment(p[i], p[i + 4]);
	if (i == 3)
		segments[j++] = segment(p[i], p[0]);
	else if (i == 7)
		segments[j++] = segment(p[i], p[4]);
	else
		segments[j++] = segment(p[i], p[i + 1]);
	}
}

vec3 QEF() {

	vec3 s = vec3(0);
	for (int i = 0; i < 12; i++)
	{
		if (segment_inter[i]) 
		{
			s.x += inter_normal[i].a.x;
			s.y += inter_normal[i].a.y;
			s.z += inter_normal[i].a.z;
			gradient += inter_normal[i].b;
		}
	}
	return vec3(s) / float(count) ;
}

vec3 DualCont(float x, float y, float z, uint thread_id) {

	Buffer.data[3 * N + unique_thread_id * 72 + thread_id * 6 + 0] = infinity;
	
	for (int i = 0; i < object_size; i++) {
		segment obj = Inter(object_list[i], segments[thread_id]);
		if (!isinf(obj.a.x)) {
			inter_normal[thread_id] = obj;
			segment_inter[thread_id] = true;
		
			Buffer.data[3 * N + unique_thread_id * 72 + thread_id * 6 + 0] = segments[thread_id].a.x;
			Buffer.data[3 * N + unique_thread_id * 72 + thread_id * 6 + 1] = segments[thread_id].a.y;
			Buffer.data[3 * N + unique_thread_id * 72 + thread_id * 6 + 2] = segments[thread_id].a.z;
			Buffer.data[3 * N + unique_thread_id * 72 + thread_id * 6 + 3] = segments[thread_id].b.x;
			Buffer.data[3 * N + unique_thread_id * 72 + thread_id * 6 + 4] = segments[thread_id].b.y;
			Buffer.data[3 * N + unique_thread_id * 72 + thread_id * 6 + 5] = segments[thread_id].b.z;
		
			atomicAdd(count, 1);
			flag = true;
			break;
		}
	}

	synchronize();

	if (!flag)
	return infinite_vec;

	return QEF();

}

void main()

{
		  if (thread_id == 0) {
		    
		    int s = 0;
		    int p = 0;
		    int co = 0;
		    int cy = 0;
		    int ob = 0;
		    int ind = 0;
		
		    while (Buffer1.data[ind] != infinity) {
		      if (int(Buffer1.data[ind]) == 1) {
		        vec3 origin = vec3(Buffer1.data[ind + 1], Buffer1.data[ind + 2], Buffer1.data[ind + 3]);
		        float radius = Buffer1.data[ind + 4];
		        sphere_list[s] = sphere(origin, radius);
		        object_list[ob] = object(1, s);
		        s++;
		        ob++;
		        ind += 5;
		      } else if (int(Buffer1.data[ind]) == 2) {
		        vec3 normal = vec3(Buffer1.data[ind + 1], Buffer1.data[ind + 2], Buffer1.data[ind + 3]);
		        vec3 a = vec3(Buffer1.data[ind + 4], Buffer1.data[ind + 5], Buffer1.data[ind + 6]);
		        vec3 b = vec3(Buffer1.data[ind + 7], Buffer1.data[ind + 8], Buffer1.data[ind + 9]);
		        vec3 c = vec3(Buffer1.data[ind + 10], Buffer1.data[ind + 11], Buffer1.data[ind + 12]);
		        vec3 d = vec3(Buffer1.data[ind + 13], Buffer1.data[ind + 14], Buffer1.data[ind + 15]);
		        plan_list[p] = plan(normal, a, b, c, d);
		        object_list[ob] = object(2, p);
		        p++;
		        ob++;
		        ind += 16;

		      } else if (int(Buffer1.data[ind]) == 3) {
		        vec3 center = vec3(Buffer1.data[ind + 1], Buffer1.data[ind + 2], Buffer1.data[ind + 3]);
		        float radius = Buffer1.data[ind + 4];
		        float height = Buffer1.data[ind + 5];
		        cylindre_list[cy] = cylindre(center, radius, height);
		        object_list[ob] = object(3, cy);
		        cy++;
		        ob++;
		        ind += 6;
		
		      } else if (int(Buffer1.data[ind]) == 4) {
		        vec3 center = vec3(Buffer1.data[ind + 1], Buffer1.data[ind + 2], Buffer1.data[ind + 3]);
		        float radius = Buffer1.data[ind + 4];
		        float height = Buffer1.data[ind + 5];
		        cone_list[co] = cone(center, radius, height);
		        object_list[ob] = object(4, co);
		        co++;
		        ob++;
		        ind += 6;
		      }
		
		    }
			
		    object_size = ob;
		    count = 0;
		    flag = false;
		    FindSegments(x, y, z);
		
		    for (int q = 0; q < 12; q++)
		      segment_inter[q] = false;
		  }
		
		  synchronize();
		
		  result = DualCont(x, y, z, thread_id);

		  Buffer.data[unique_thread_id] = result.x;
		  Buffer.data[unique_thread_id + N] = result.y;
		  Buffer.data[unique_thread_id + 2 * N] = result.z;

}