# Proiect GlobalWaves  - Etapa 3

###### Lungu Andrei-Daniel, 323CD

<div align="center"><img src="https://tenor.com/view/listening-to-music-spongebob-gif-8009182.gif" width="300px"></div>

## Descrierea implementarii:

### Am folosit ca schelet rezolvarea proprie, aceeasi de la etapele 1 si 2.

#### Design pattern-uri folosite: Singleton, Visitor, Factory, Strategy, Observer
1. Singleton: 
Am folosit Singleton pentru CommandManager. Pentru Admin tot un fel de singleton am folosit, dar
ea nu poate fi instantiata deloc, continand doar campuri si metode statice.

2. Visitor:
Functia printCurrentPage din GeneralUser se comporta ca o functie visit, iar functia printPage din
interfata Page se comporta ca o functie accept. UserIncome are metoda paySongs, iar ArtistIncome
sellSongs, pe acelasi principiu.

3. Factory:
Functiile createUser si delete din clasa GeneralUser au un comportament specific design-ului factory.
Pentru ca trebuia facuta o verificare in library am ales ca din createUser sa adaug direct noua
instanta creata in lista corespunzatoare din library. Nu sunt adaugati utilizatori in alt fel decat
apeland metoda createUser, ceea ce inseamna ca e un Factory.
createUser se comporta ca un constructor pentru un tip general de User. Alta metoda care se foloseste
de acest principiu este getUserOfTypeOrWriteMessage din clasa Admin.

4. Strategy:
Interfata AudioObject si clasele abstracte AudioFile si AudioCollection care sunt folosite pentru player
fac extinderea prin adaugarea unor tipuri noi de obiecte audio acceptate de player mai usoara. Tot ce
trebuie sa indeplineasca acest tip nou de sursa e sa implementeze metodele necesare playerului, mai ales
getTracks si getDuration. Tot Strategy se foloseste si la interfetele Page si Wrapped.

5. Observer:
Artist si Host sunt observatori, avand metoda sendNotifications, echivalenta cu "notifyAll". User e 
observabil, avand metoda receiveNotification. Metoda de a adauga un observator e cea de subscribe / unsubscribe,
care ofera o relatie simetrica intre cele doua. Metodele sunt impuse de interfetele Observer si 
Observable (nu am putut face clase abstracte deoarece aveam deja o mostenire). Pentru a simplifica lucrul
am adaugat o lista subscriptions in clasa GeneralUser care pentru user inseamna la cine e abonat si pentru
ceilalti sunt abonatii lor. Intr-un alt sens, Playerul e observator pentru UserStats (ii trimite updateStats),
la randul lui UserStats e observator pentru UserIncome(ii trimite updateMonetization).

#### Detalii de design:
1. In mai multe locuri am ales sa fac compunere reciproca intre doua clase, adaugand ca parametru la 
constructorul unei clase pe cealalta. Astfel, nu poate exista o pagina de user fara un owner, nu poate
exista un player, un searchBar, o clasa de Wrapped, etc. fara a fi ale cuiva. Aceasta relatie e utila si
pentru ca metodele din clase se folosesc de informatii despre owner.

2. Mosteniri / implementari

Am adaugat superclasa GeneralUser peste User, Host si Admin. Aceasta are rol de Factory pentru useri si e de ajutor
oricand se interactioneaza cu useri (fie la search, fie la adaugare/stergere).

Paginile implementeaza interfata Page, care impune existenta unei metode printPage, folosita de comanda printCurrentPage.

Statisticile pentru fiecare user mostenesc interfata Wrapped, care le pune la dispozitie si cateva metode statice
pentru prelucrarea datelor.

Artist si Host implementeaza Observator (ca un canal de youtube), iar Userul normal implementeaza Observable (nu
cred ca le-am pus numele tocmai standard acestor interfete).

#### Alte detalii de implementare:

3. Generics

In interfata Wrapped am adaugat mai multe functii statice generice si comparatori (trebuie sa recunosc ca aici
Chat GPT are meritul de a ma fi ajutat cu sintaxa). Astfel construirea statisticii pentru fiecare tip de user a
fost mult mai usor de scris. Am vazut utilitatea acestor functii si am rescris si metodele printPage dupa ce am
adaugat in clasa Page metode generice.

4. Colectii si stream-uri

Am folosit mai toate tipurile de colectii, fiecare pentru propritatile ei: la HostStats am folosit HashSet,
in multe alte locuri am folosit HashMap si ArrayList, dar am avut nevoie si de LinkedHashMap pentru ordine, 
uneori de TreeSet daca obiectele erau comparabile (in endProgram), LinkedList, etc. Am folosit multe
stream-uri pentru a-mi usura munca.

5. Am incercat sa evit duplicarea codului, motiv pentru care am dat objectNode ca parametru si nu ca valoare de return
functiilor. Tot in acest sens am incercat sa adaug fiecarei clase o functionalitate utila care sa poate fi accesata
din exterior (cum ar fi delete pentru useri si toString-urile pentru crearea paginilor).


#### Implementari suplimentare

Am implementat si lucruri pe care testele nu le verifica si pe care nici enuntul nu le defineste
in totalitate, cum ar fi: 

1. notificari pentru follow/unfollow playlist (v. functia follow/unfollow din Playlist).

2. imposibilitatea de a da next/prev daca un ad ruleaza

3. tratarea (cel putin teoretica, nu e perfecta) a cazurilor in care s-ar folosi repeat/shuffle si ar fi anunturi in player,
incat sa functioneze wrapped si monetizarea


#### Observatii la final de proiect:

Cred ca puteam gandi mai bine Playerul, e foarte rigid si greu de lucrat cu el si a ajuns sa aiba multe linii.

Pentru comenzile care au output trebuia sa fi creat un design pattern mai aproape de Command sau sa fi fost toate 
administrate de clasa Admin, reducand din fiecare clasa partea care scrie efectiv in ObjectNode.

Acum era tarziu sa fac astfel de modificari asa ca am continuat in aceeasi logica, incercand sa distribui comenzile
in clasa care ma avantaja cel mai mult, daca as fi descoperit de la inceput ideea de compunere reciproca as fi putut
face mult mai curat codul.



