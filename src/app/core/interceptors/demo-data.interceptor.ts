import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse
} from '@angular/common/http';
import { Observable, of } from 'rxjs';

@Injectable()
export class DemoDataInterceptor implements HttpInterceptor {
  private readonly tutors = [
    {
      id: 101,
      fullName: 'Amine Benali',
      specialite: 'Mathématiques',
      tarifHoraire: 35,
      noteMoyenne: 4.8,
      nombreEvaluations: 64,
      verifie: true,
      disponible: true,
      coursEnLigne: true,
      coursPresentiel: true,
      ville: 'Casablanca',
      pays: 'Maroc',
      description: 'Spécialiste du lycée et préparation examens'
    },
    {
      id: 102,
      fullName: 'Sara Lahrach',
      specialite: 'Physique',
      tarifHoraire: 30,
      noteMoyenne: 4.6,
      nombreEvaluations: 39,
      verifie: true,
      disponible: true,
      coursEnLigne: true,
      coursPresentiel: false,
      ville: 'Rabat',
      pays: 'Maroc',
      description: 'Cours interactifs et méthodologie scientifique'
    },
    {
      id: 103,
      fullName: 'Youssef El Idrissi',
      specialite: 'Programmation',
      tarifHoraire: 42,
      noteMoyenne: 4.9,
      nombreEvaluations: 71,
      verifie: true,
      disponible: false,
      coursEnLigne: true,
      coursPresentiel: true,
      ville: 'Fès',
      pays: 'Maroc',
      description: 'Python, Java, Angular et accompagnement projets'
    }
  ];

  private readonly subjects = [
    { id: 1, nom: 'Mathématiques', description: 'Algèbre, analyse et géométrie', niveau: 'INTERMEDIAIRE', domaine: 'Sciences' },
    { id: 2, nom: 'Physique', description: 'Mécanique, électricité, optique', niveau: 'AVANCE', domaine: 'Sciences' },
    { id: 3, nom: 'Programmation Web', description: 'HTML, CSS, TypeScript, Angular', niveau: 'DEBUTANT', domaine: 'Informatique' },
    { id: 4, nom: 'Anglais', description: 'Communication orale et écrite', niveau: 'INTERMEDIAIRE', domaine: 'Langues' }
  ];

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    if (localStorage.getItem('demo_mode') !== 'true') {
      return next.handle(req);
    }

    const url = req.url.toLowerCase();

    if (req.method === 'GET') {
      if (url.includes('/api/auth/profile')) {
        return this.ok({
          id: 1,
          nom: 'Admin',
          prenom: 'Demo',
          email: 'demo.admin@edututor.dev',
          role: 'ADMIN',
          userType: 'ADMIN',
          roles: ['ADMIN'],
          isAuthenticated: true
        });
      }

      if (url.includes('/api/tutors/statistics')) {
        return this.ok({
          totalTutors: this.tutors.length,
          activeTutors: this.tutors.length,
          availableTutors: this.tutors.filter(t => t.disponible).length,
          verifiedTutors: this.tutors.filter(t => t.verifie).length,
          averageRating: 4.77,
          popularSpecialties: ['Mathématiques', 'Programmation', 'Physique'],
          popularCities: ['Casablanca', 'Rabat', 'Fès'],
          averageHourlyRate: 35,
          tutorsWithRatings: this.tutors.length
        });
      }

      if (url.includes('/api/tutors/available')) {
        return this.ok(this.tutors.filter(t => t.disponible));
      }

      if (url.includes('/api/tutors/verified')) {
        return this.ok(this.tutors.filter(t => t.verifie));
      }

      if (url.includes('/api/tutors/search') || url.endsWith('/api/tutors')) {
        return this.ok(this.tutors);
      }

      if (url.match(/\/api\/tutors\/\d+$/)) {
        const id = Number(url.split('/').pop());
        const tutor = this.tutors.find(t => t.id === id) || this.tutors[0];
        return this.ok({
          id: tutor.id,
          nom: tutor.fullName.split(' ')[1] || tutor.fullName,
          prenom: tutor.fullName.split(' ')[0] || 'Demo',
          email: `tutor${tutor.id}@edututor.dev`,
          statut: 'ACTIVE',
          dateInscription: new Date().toISOString(),
          experience: '5 ans',
          tarifHoraire: tutor.tarifHoraire,
          diplomes: 'Master',
          description: tutor.description,
          specialites: [{
            id: 1,
            tutorId: tutor.id,
            matiereId: 1,
            matiereNom: tutor.specialite,
            matiereDescription: tutor.specialite
          }],
          noteMoyenne: tutor.noteMoyenne,
          nombreEvaluations: tutor.nombreEvaluations,
          verifie: tutor.verifie,
          disponible: tutor.disponible,
          coursEnLigne: tutor.coursEnLigne,
          coursPresentiel: tutor.coursPresentiel,
          ville: tutor.ville,
          pays: tutor.pays,
          fullName: tutor.fullName,
          availableForBooking: tutor.disponible
        });
      }

      if (url.includes('/api/matieres/domains')) {
        return this.ok(Array.from(new Set(this.subjects.map(s => s.domaine))));
      }

      if (url.includes('/api/matieres/count/domain/')) {
        const domain = decodeURIComponent(req.url.split('/').pop() || '').toLowerCase();
        const count = this.subjects.filter(s => s.domaine.toLowerCase() === domain).length;
        return this.ok(count);
      }

      if (url.includes('/api/matieres/exists')) {
        const name = (req.params.get('nom') || '').toLowerCase();
        return this.ok(this.subjects.some(s => s.nom.toLowerCase() === name));
      }

      if (url.includes('/api/matieres/search') || url.endsWith('/api/matieres')) {
        const searchTerm = (req.params.get('searchTerm') || '').toLowerCase();
        const filtered = searchTerm
          ? this.subjects.filter(s => s.nom.toLowerCase().includes(searchTerm) || s.description.toLowerCase().includes(searchTerm))
          : this.subjects;
        return this.ok(filtered);
      }

      if (url.match(/\/api\/matieres\/\d+$/)) {
        const id = Number(url.split('/').pop());
        const subject = this.subjects.find(s => s.id === id) || this.subjects[0];
        return this.ok(subject);
      }
    }

    if (req.method === 'POST' && url.endsWith('/api/matieres')) {
      const body = req.body as any;
      return this.ok({
        id: Date.now(),
        nom: body?.nom || 'Nouvelle matière',
        description: body?.description || '',
        niveau: body?.niveau || 'INTERMEDIAIRE',
        domaine: body?.domaine || 'Général'
      });
    }

    if (req.method === 'PUT' && url.match(/\/api\/matieres\/\d+$/)) {
      return this.ok(req.body || {});
    }

    if (req.method === 'DELETE' && url.match(/\/api\/matieres\/\d+$/)) {
      return of(new HttpResponse({ status: 200 }));
    }

    return next.handle(req);
  }

  private ok(body: unknown): Observable<HttpEvent<unknown>> {
    return of(new HttpResponse({ status: 200, body }));
  }
}
