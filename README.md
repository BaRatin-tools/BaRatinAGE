# BaRatinAGE
_BaRatin Advanced Graphical Environment_

[English version follows > ](#Overviews)

## Présentation général

BaRatin (BAyesian RATINg curve) est une méthode d'estimation des courbes de tarage et des incertitudes associées à l'aide d'un formalisme bayésien. Ce dépôt contient l'interface graphique de BaRatin, nommée BaRatinAGE.

## Téléchargement et installation

Pour télécharger la dernière version de **BaRatinAGE**, rendez vous sur la page [https://github.com/BaRatin-tools/BaRatinAGE/releases](https://github.com/BaRatin-tools/BaRatinAGE/releases).
Choisissez l'archive .zip version adaptée à votre système d'exploitation (Windows ou Linux). 
Par exemple, pour Windows, et pour la version 2.2, allez dans la section **BaRatinAGE 2.2** cliquez sur **BaRatinAGE_v2.2_windows.zip** pour télécharger l'archive.

**BaRatinAGE** ne nécéssite aucune installation.
Il vous suffit de dézipper / décompresser l'archive .zip dans le répertoir de votre choix. 

_Note: contairement aux anciennes version de BaRatinAGE, les nouvelles versions ne nécessitent plus l'installation de Java._

## Première utilisation

Pour lancer **BaRatinAGE**: 
- sur windows, à la racine du dossier contenant BaRatinAGE que vous venez de décompresser, il vous suffit de cliquer sur l'executable nommé **BaRatinAGE.exe** qui s'y trouve.
- sur linux:
    - dans le sous dossier `bin\`  du dossier contenant BaRatinAGE que vous venez de décompresser,il vous suffit de cliquer sur l'executable nommé **BaRatinAGE** qui s'y trouve.
    - il est fréquent de devoir changer les droits d'execution de l'executable. En ligne de commande: `sudo chmod a=rx BaRatinAGE`.
    - la même manipulation est parfois nécessaire avec l'executable de **BaRatin** qui se trouve dans le répertoir `bin\exe\`

## Aide

Pour acceder à l'aide de **BaRatinAGE** ouvrez le fichier `help\fr\index.html` (ou `bin\help\fr\index.html` sur linux).
Vous y trouverez beaucoup d'informations très utiles sur l'utilisation de **BaRatinAGE**.

Vous pouvez également poser vos questions en envoyant un mail à **baratin.dev [at] lists.irstea.fr**.

## Contributions

### Bugs

Si vous rencontrez des problèmes ou bugs lors de l'utilisation de **BaRatinAGE** n'hesitez pas nous en faire part.
La meilleurs solution est d'avoir  un compte Guthub et de créer une nouvelle _issue_  en cliquant sur `New Issue` sur la page [https://github.com/BaRatin-tools/BaRatinAGE/issues](https://github.com/BaRatin-tools/BaRatinAGE/issues).
Alternativement, vous pouvez nous envoyer un mail à l'adresse **baratin.dev [at] lists.irstea.fr**.
Dans tous les cas soyez le plus exhaustif possible dans la description du problème.

### Multilinguisme

Les contributions pour traduire BaRatinAGE vers d'autres langues sont bienvenues! En particulier pour la traduction de l'aide de BaRatinAGE.
Les fichiers à traduire sont les suivants:
1. Le fichier `lang/dico.txt`. Ajoutez simplement une colonne contenant la traduction de tous les termes dans la nouvelle langue. N'utilisez ni virgules ni points-virgules dans les traductions, car ces caractères sont utilisés comme séparateurs.
2. Tous les fichiers `.html` dans le dossier `help`. Aucune connaissance du langage HTML n'est nécessaire. Ouvrez simplement le fichier avec un éditeur de texte (par exemple Notepad++) et traduisez le texte entre les balises HTML
   (<balise> texte à traduire </balise>). Ne modifiez pas les balises elles-mêmes!

Vous pouvez envoyer vos contributions en ouvrant un nouvelle _issue_ sur Github (`New Issue` sur la page [https://github.com/BaRatin-tools/BaRatinAGE/issues](https://github.com/BaRatin-tools/BaRatinAGE/issues), nécéssite un compte Github) ou par email à l'adresse **baratin.dev [at] lists.irstea.fr**.

### Developpement

Pour plus d'information sur le développement de BaRatinAGE, referez vous au fichier `CONTRIBUTING.md` (anglais uniquement) sur la page Github  [https://github.com/BaRatin-tools/BaRatinAGE/](https://github.com/BaRatin-tools/BaRatinAGE/).


---

## Overview 

BaRatin (BAyesian RATINg curve) is a Bayesian approach to the estimation of rating curves and associated uncertainties. This repository contains BaRatin graphical interface, named BaRatinAGE.

## Download and installation

To dowload the latest version of **BaRatinAGE**, go to the page [https://github.com/BaRatin-tools/BaRatinAGE/releases](https://github.com/BaRatin-tools/BaRatinAGE/releases).
Choose the .zip archive which matches your operating system (windows or linux). 
For example, for Windows, and version 2.2, got to the section **BaRatinAGE 2.2** click on **BaRatinAGE_v2.2_windows.zip** to download the archive.

**BaRatinAGE** doesn't require any installation.
You simply need to unzip / uncompress the .zip archive in the repertory of your choice.

_Note: unlike previous version of BaRatinAGE, new vesrions do not require the installation of Java._

## Getting started

To execute **BaRatinAGE**:
- on Windows, at the root of the folder which contains **BaRatinAGE** (that you've just uncompressed), click on the executable file named **BaRatinAGE.exe**.
- on Linux:
    - in the sub folder `bin\` of the folder containing the unzipped **BaRatinAGE**,  click on the executable file nammed **BaRatinAGE**.
    - it is common to have to modify executition rights on the file before. Using the terminal, run `sudo chmod a=rx BaRatinAGE`.
    - the same step is often require for the **BaRatin** executable in the folder `bin\exe\`.

## Help

To access the help of **BaRatinAGE** open the file `help\fr\index.html` (or `bin\help\fr\index.html` on Linux).

You also can reach out and ask you questions by sending an email to **baratin.dev [at] lists.irstea.fr**.

## Contributions

### Bugs

If you enconter bugs or any problems when using **BaRatinAGE**, please let us know.
The best way to do so is to have a Guithub accompt and open a new _issue_ by clicking `New Issue` on the page la page [https://github.com/BaRatin-tools/BaRatinAGE/issues](https://github.com/BaRatin-tools/BaRatinAGE/issues).
Alternatively, you can send an emeil to  **baratin.dev [at] lists.irstea.fr**.
Please, be as exhaustive as possible when describing the issue.

### Multi-language support

Contributions to translate BaRatinAGE are welcome! In particular for the help of **BaRatinAGE**.
The files needing translation are the following:
1. File `lang/dico.txt`. Just add a column including the translation of all terms in the new language.
   Please do not use commas or semicolons in your translation, since these characters are used as separators.
2. All `.html ` files in the `help` folder. No knowledge of the HTML language is required: simply open the file with a text editor (e.g. Notepad++) and translate all text located within HTML tags(<tag> text needing translation </tag>). Please do not modify the tags themselves!

Please send your contributions by opening a new _issue_ on Github (`New Issue` on the page [https://github.com/BaRatin-tools/BaRatinAGE/issues](https://github.com/BaRatin-tools/BaRatinAGE/issues) or by sending an email to baratin.

### Developpement

For more information on BaRatinAGE delelopment, please see the file `CONTRIBUTING.md` on the github page [https://github.com/BaRatin-tools/BaRatinAGE/](https://github.com/BaRatin-tools/BaRatinAGE/).
