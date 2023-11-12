# BritaCard
## Architecture
### Fonction hello
Pour écrire notre première instruction, nous nous sommes basés sur la commande HelloWord du premier TP. Cela nous a permit, au départ, de tester l'envoie et la réception d'APDU via scriptor.
### PIN
Un PIN est défini en dur dans l'applet.
### RSA encryption
Au lancement de l'applet, nous générons une paire de clef RSA de taille 512 bits.
Pour signer le message, on utilise SHA1 de taille (160 bits).
### Instruction
| `CLA` | `INS` | Description                                                                      | Authentification nécessaire | Entrée   | Sortie                                  |
|-------|-------|----------------------------------------------------------------------------------|-----------------------------|----------|-----------------------------------------|
| 10    | 01    | Utilisé tester le bon fonctionnement de l'applet. Renvoie un message hello world |                             |          | Message (8o)                            |
| 10    | 02    | Fonction de login                                                                |                             | PIN (4o) |                                         |
| 10    | 03    | Renvoie la clé publique                                                          | Oui                         |          | clé publique (exposant 3o + modulo 64o) |
| 10    | 04    | Signe un message                                                                 | Oui                         | Message  | Signature (64o)                         |

### APDU
```
00 A4 04 00 0B A0 00 00 00 62 03 01 0C 06 01 02 -> Selection de l'applet
10 01 00 00 00 -> INS HELLO
10 01 00 00 08 -> Retour d'HelloWord avec un Le = 8 Octets
10 02 00 00 04 PIN -> INS LOGIN (insertion du PIN, voir section client)
10 C0 00 00 03 -> Lecture de l'APDU de taille 3 Octets
10 04 00 00 03 0a 0b 0c -> INS SIGNMSG
10 C0 00 00 40 -> Lecture de l'APDU contenant la signature
10 03 00 00 00 -> INS GETPUBKEY
10 03 00 00 47 -> Lecture de l'APDU contenant le module (40o), l'exposant (3o) et les tailles du modules et exposant (2o)
```

## Tester l'applet
Avant de développer notre client en **bash**, nous avons testé notre applet avec le binaire scriptor qui permet d'envoyer et recevoir des APDU facilement.
Exemple de script :
```
00 A4 04 00 0B A0 00 00 00 62 03 01 0C 06 01 02
10 01 00 00 00
10 01 00 00 08
```

## Client
### Java
Nous avons d'abord tenté d'implémenter un client en Java (trouvable dans `client/src/`).

Après sélection de l'applet et malgré le bon formattage des APDU envoyés, celui-ci retourne systématiquement l'erreur `6d00` (Instruction code not supported or invalid).

De longues heures de débogage ne nous ont pas permis de régler ce problème.

### Bash
Nous avons ensuite pris le parti de faire un nouveau client en bash (`client.sh`), avec en tête l'utilisation de scriptor, openssl etc pour vérifier le fonctionnement et la signature d'un message par notre applet.
Notre client se compose d'un shell intéractif. Les intéractions possibles sont :

- **check**: vérifier que la JavaCard est connectée.
- **compile**: compiler l'applet.
- **upload**: téléverser l'applet compilé vers la JavaCard.
- **cu**: compiler puis téléverser l'applet compilé vers la JavaCard.
- **sendN**: demande à l'applet de signer un message et vérifie cette signature avec la clé publique de la JavaCard.

### PIN
Scriptor n'étant pas intéractif, nous avons dû créer un fichier d'instruction "template" permettant lors de la saisie du PIN par l'utilisateur, de remplacer cette valeur dans le fichier.
Template :
```
00 A4 04 00 0B A0 00 00 00 62 03 01 0C 06 01 02
10 01 00 00 00
10 01 00 00 08
10 02 00 00 04 PIN
```
Avec remplacement du PIN :
```
00 A4 04 00 0B A0 00 00 00 62 03 01 0C 06 01 02
10 01 00 00 00
10 01 00 00 08
10 02 00 00 04 09 08 06 06
```

# Installation
```bash
$ chmod u+x client.sh 
$ ./client.sh 
```

**ENJOY**