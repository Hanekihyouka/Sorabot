#include <stdio.h>
#include <math.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>

#define random(a, b) (rand()%(b-a+1)+a)
#define randomaze srand((unsigned)time(NULL))
#define DEFEND 2
#define EVADE 3
#define STRATEGYSIZE 31
#define STRINGLENGTH 50
#define ITERATETIME 10
#define SIMTIME 10000

int max(int a, int b)
{
	if (a > b) return a;
	else return b;
}

int min(int a, int b)
{
	if (a < b) return a;
	else return b;
}

//向下是骰子实现 
class Dice
{
	public:
		int chance[STRINGLENGTH]; //对应骰子点数的出现几率 
		int maxP;
		int minP;
		int condcount;
		void Set_Chance(int die[], int size);
		void Show(void);
};

void Dice::Set_Chance(int die[], int size)
{
	int i;
	maxP = -1;
	minP = 99;
	condcount = 0;
	for (i = 0; i <= size; i++)
	{
		chance[i] = die[i];
		if (die[i] != 0 && i > maxP) maxP = i;
		if (die[i] != 0 && i < minP) minP = i;
		condcount += die[i];
	}
}

void Dice::Show(void)
{
	int i;
	for(i = minP; i <= maxP; i++) printf("%d\t%d\n", i, chance[i]);
}

int COMMON[7] = {0, 1, 1, 1, 1, 1, 1};
int D2GETHI[7] = {0, 1, 3, 5, 7, 9, 11};
int L7[8] = {1, 1, 1, 1, 1, 1, 1, 1};
int DESPMODI[7] = {0, 1, 0, 0, 0, 0, 1};
int EXORSPEC[7] = {0, 0, 0, 0, 0, 0, 1};
int TALEAWAK[7] = {0, 0, 0, 0, 0, 1, 0};
int DELTA[2] = {0, 1}; 
Dice CommonDice, LuckyDice, DespairDice, ExordinaryDice, TalentDice, DeltaDice, WDice;

Dice Dice_Combine(Dice DiceA, Dice DiceB) //投掷两枚骰子的点数 
{
	Dice DiceC;
	int i, j;
	DiceC.minP = DiceA.minP + DiceB.minP;
	DiceC.maxP = DiceA.maxP + DiceB.maxP;
	DiceC.condcount = DiceA.condcount * DiceB.condcount;
	memset(DiceC.chance, 0, sizeof(DiceC.chance));
	for(i = DiceA.minP; i <= DiceA.maxP; i++)
		for(j = DiceB.minP; j <= DiceB.maxP; j++)
			DiceC.chance[i + j] += DiceA.chance[i] * DiceB.chance[j];
	return DiceC;
}

void DiceReset()
{
	CommonDice.Set_Chance(COMMON, 6);
	LuckyDice.Set_Chance(L7, 7);
	DespairDice.Set_Chance(DESPMODI, 6);
	ExordinaryDice.Set_Chance(EXORSPEC, 6);
	TalentDice.Set_Chance(TALEAWAK, 6);
	DeltaDice.Set_Chance(DELTA, 1);
	WDice = Dice_Combine(CommonDice, CommonDice);
}
//向上是骰子实现 

struct CharaData
{
	char name[STRINGLENGTH];
	int MHP;
	int ATK;
	int DEF;
	int EVD;
	int passive;
};

class Character
{
	public:
		char name[STRINGLENGTH];
		int MHP; //Max HP
		int HP;
		int ATK;
		int DEF;
		int EVD;
		int EXD; //Extra Damage
		int EXR; //Extra Resistance
		int tHP;
		int tATK; //t表示临时，因其它因素改变后的属性
		int tDEF;
		int tEVD;
		int passive;
		int carduse;
		int PSIV_TRIG; //特殊被动开关 
		int passive_depth; //被动深度，指被动实际可能触发的最高层数。 
		Dice Adice;
		Dice DEdice;
		int strategy_space[STRATEGYSIZE][STRATEGYSIZE][STRATEGYSIZE]; //HP, _A, _HP
		int unused_strategy[STRATEGYSIZE][STRATEGYSIZE][STRATEGYSIZE];
		int str_size[6];
		void Show_Data(void);
		void Set_Passive(int psiv); 
		void Set_Dice(char dicename[]);
		void Set_Stat(int nMHP, int nATK, int nDEF, int nEVD);
		int Edit_Stat(char attritype[], int value);
		void Load_CharaData(CharaData CD);
		void Strategy_Build(int _HP);
		void Reset_Temp(int FS, Character *_Character);
		int DEJudge(int HP, int _A, int _HP);
		void toggleDE(int HP, int _A, int _HP);
		void Print_Strategy(void);
}; 

void Character::Show_Data(void)
{
	printf("%s\n", name);
	printf("(%d/%dHP %d(+%d) %d(+%d) %d REC-)\n", HP, MHP, ATK, EXD, DEF, EXR, EVD);
	printf("Passive:%d(%d)\n", passive, passive_depth);
	printf("Card:%s\n", carduse);
	printf("Dice:\n");
	Adice.Show();
}

void Character::Set_Passive(int psiv)
{
	passive = psiv;
	if (passive == 1100) passive_depth = 3; //Cook
	if (passive == 8080) passive_depth = 2; //Emerangler
	if (passive == 8090) passive_depth = 9; //Bunnizard
	if (passive == 8100) passive_depth = 9; //Trollite
	if (passive == 8110) passive_depth = 2; //Terrawyrmer
	if (passive == 8120) passive_depth = 9; //Globbu
}

void Character::Set_Dice(char dicename[])
{
	if (strcmp(dicename, "Common") == 0)
	{
		Adice = CommonDice;
		DEdice = CommonDice;
	}
	else if (strcmp(dicename, "L7") == 0) 
	{
		Adice = LuckyDice;
		DEdice = LuckyDice;
	}
	else if (strcmp(dicename, "DES") == 0)
	{
		Adice = DespairDice;
		DEdice = DespairDice;
	}
	else if (strcmp(dicename, "EXO") == 0) 
	{
		Adice = ExordinaryDice;
		DEdice = ExordinaryDice;
	}
	else if (strcmp(dicename, "TAL") == 0)
	{
		Adice = TalentDice;
		DEdice = TalentDice;
	}
	else if (strcmp(dicename, "DEL") == 0)
	{
		Adice = DeltaDice;
		DEdice = DeltaDice;
	} 
	else if (strcmp(dicename, "Double") == 0)
	{
		Adice = WDice;
		DEdice = WDice;
	}
	else if (strcmp(dicename, "ACCH") == 0)
	{
		Adice = WDice;
		DEdice = CommonDice;
	}
}

void Character::Set_Stat(int nMHP, int nATK, int nDEF, int nEVD)
{
	MHP = nMHP;
	HP = MHP;
	ATK = nATK;
	DEF = nDEF;
	EVD = nEVD;
}

int Character::Edit_Stat(char attritype[], int value)
{
	if (strcmp(attritype, "HP") == 0) 
	{
		if (value <= 0 || value > 20) return 2;
		HP = value;
	}
	else if (strcmp(attritype, "MHP") == 0)
	{
		if (value <= 0 || value > 20) return 2;
		MHP = value;
	}
	else if (strcmp(attritype, "ATK") == 0)
	{
		if (value > 9) value = 9;
		if (value < -9) value = -9;
		ATK = value;
	}
	else if (strcmp(attritype, "DEF") == 0)
	{
		if (value > 9) value = 9;
		if (value < -9) value = -9;
		DEF = value;
	}
	else if (strcmp(attritype, "EVD") == 0)
	{
		if (value > 9) value = 9;
		if (value < -9) value = -9;
		EVD = value;
	}
	else if (strcmp(attritype, "EXD") == 0) EXD = value;
	else if (strcmp(attritype, "EXR") == 0) EXR = value;
	else return 1;
	return 0;
}

void Character::Load_CharaData(CharaData CD)
{
	MHP = CD.MHP;
	HP = MHP;
	ATK = CD.ATK;
	DEF = CD.DEF;
	EVD = CD.EVD;
	EXD = 0;
	EXR = 0;
	strcpy(name, CD.name);
	passive = CD.passive;
	carduse = 0;
	passive_depth = 0;
	if (passive == 1100) passive_depth = 3; //Cook
	if (passive == 8080) passive_depth = 2; //Emerangler
	if (passive == 8090) passive_depth = 9; //Bunnizard
	if (passive == 8100) passive_depth = 9; //Trollite
	if (passive == 8110) passive_depth = 2; //Terrawyrmer
	if (passive == 8120) passive_depth = 9; //Globbu
	Set_Dice("Common");
}

void Character::Strategy_Build(int _HP)
{
	int i, j, k;
	memset(strategy_space, 0, sizeof(strategy_space));
	memset(unused_strategy, 0, sizeof(unused_strategy));
	str_size[1] = HP;
	str_size[2] = EVD + DEdice.maxP - 1;
	str_size[3] = _HP;
	for(i = 1; i <= str_size[1]; i++)
	for(j = 1; j <= str_size[2]; j++)
	for(k = 1; k <= str_size[3]; k++)
		strategy_space[i][j][k] = DEFEND;
}

void Character::Reset_Temp(int FS, Character *_Character)
{
	tHP = HP;
	tATK = ATK;
	tDEF = DEF;
	tEVD = EVD;
	EXD = 0;
	EXR = 0;
	PSIV_TRIG = 0;
	if (passive == 2050) //Syura
	{
		if (tHP = 1)
		{
			tATK++;
			tEVD++;
		}
	}
	if (passive == 4070) //Kae
	{
		if (FS == 1) tDEF--;
		tATK -= tDEF;
	}
	if (_Character->passive == 4100) //Iru
	{
		if (FS == 2) tHP--;
		if (passive == 6070) tATK++; //Tequila 
		if (passive == 6071) PSIV_TRIG = 1; //TequilaBH
	}
	if (passive == 6050) //Castle
	{
		if (FS == 2)
		{
			tATK = 99;
			EXD = -198; //ATK极大防止对手触发反伤，EXD极小防止造成伤害 
		}
	}
	if (passive == 7030) //Rone
	{
		if (_Character->tATK >= 1 && _Character->tATK < 10) tEVD++;
		if (_Character->tATK >= 2 && _Character->tATK < 10) tEVD++;
	}
	if (passive == 8030) //Gost
	{
		if (_Character->tATK >= 1 && _Character->tATK < 10) tEVD += _Character->tATK;
	}
	if (_Character->passive == 8050) //Turnislime
	{
		if (tATK >= 2 && tATK < 10) tATK--;
		tATK--;
	}
	if (passive == 8070) //Wolly
	{
		EXR = -1;
	}
}

int Character::DEJudge(int HP, int _A, int _HP) //闪防判断 
{
	if (passive == 4080) return DEFEND; //Kyoko
	if (_A == 0) return EVADE;
	if (_A >= EVD + DEdice.maxP) return DEFEND;
	return strategy_space[HP][_A][_HP];
}

void Character::toggleDE(int HP, int _A, int _HP)
{
	strategy_space[HP][_A][_HP] = 5 - strategy_space[HP][_A][_HP];
}

void Character::Print_Strategy(void)
{
	int i, j, k;
	for(i = 1; i <= str_size[1]; i++)
	{
		for(j = 1; j <= str_size[2]; j++)
		{
			for(k = 1; k <= str_size[3]; k++)
			{
				if (unused_strategy[i][j][k] == 1) printf("×");
				else if (strategy_space[i][j][k] == DEFEND) printf("■");
				else if (strategy_space[i][j][k] == EVADE) printf("□");
				else printf("ER");
			}
			printf(" ");
		}
		printf("\n");
	}	
}

CharaData CommonChar[10000];
Character CharaA, CharaB;
int condition_space[STRATEGYSIZE][STRATEGYSIZE];
float winrate_space[STRATEGYSIZE][STRATEGYSIZE];
float fb_winrate_space[STRATEGYSIZE][STRATEGYSIZE][STRATEGYSIZE];
float rate_Win, rate_Lose, rate_Draw;

void Get_CondSpace(Character *CharaA, Character *CharaB, int HP, int _HP, int dice_condcount_BF) //战斗处理主函数 
{
	int m, n, p, q;
	int AF, AS, D, E;
	int Damage;
	int CommonDamage_F, CommonDamage_S, SpecialDamage_F, SpecialDamage_S;
		
	memset(condition_space, 0, sizeof(condition_space));
	for(m = CharaA->Adice.minP; m <= CharaA->Adice.maxP; m++) //m为A攻击骰 
	{
		if (CharaA->passive == 6070) AF = max(0, CharaA->tATK + (CharaA->tHP - HP) + m);
		else if (CharaA->passive == 6071 && (CharaA->PSIV_TRIG == 1 || HP < CharaA->tHP)) AF = max(0, CharaA->tATK + 1 + m); //Tequila
		else AF = max(0, CharaA->tATK + m);
		if (m != 0) AF = max(1, AF); //任意点数最低为0点，投点不为0时则最低为1点
		if (CharaB->DEJudge(_HP, AF, HP) == DEFEND) for(n = CharaB->DEdice.minP; n <= CharaB->DEdice.maxP; n++) //n为B防御骰 
		{
			D = max(0, CharaB->tDEF + n);
			if (n != 0) D = max(1, D);
			Damage = max(1, AF - D); //防御时最少受到1点伤害
			CommonDamage_F = max(0, Damage + CharaA->EXD - CharaB->EXR); //考虑上额外伤与额外抗后的常规伤害
			if (CharaB->passive == 7020) CommonDamage_F = min(2, CommonDamage_F); //miusaki
			SpecialDamage_S = 0; //反伤等由被动方造成的伤害
			if (SpecialDamage_S != 0) SpecialDamage_S = max(0, SpecialDamage_S + CharaB->EXD - CharaA->EXR); //考虑上额外伤与额外抗后的特殊伤害
			CommonDamage_F = min(_HP, CommonDamage_F);
			SpecialDamage_S = min(HP, SpecialDamage_S); //受到伤害最多为血量，防止溢出
			if (CommonDamage_F == _HP) condition_space[HP][0] += CharaA->Adice.chance[m] * CharaB->DEdice.chance[n] * dice_condcount_BF; //后攻方被击倒，不需进行下个攻防
			else if (SpecialDamage_S == HP) condition_space[0][_HP] += CharaA->Adice.chance[m] * CharaB->DEdice.chance[n] * dice_condcount_BF; //先攻方被反倒，不需进行下个攻防
			else for(p = CharaB->Adice.minP; p <= CharaB->Adice.maxP; p++) //p为B攻击骰
			{
				if (CharaB->passive == 6070) AS = max(0, CharaB->tATK + (CharaB->tHP - _HP + CommonDamage_F) + p);
				else if (CharaB->passive == 6071 && (CharaB->PSIV_TRIG == 1 || (_HP - CommonDamage_F) < CharaB->tHP)) AS = max(0, CharaB->tATK + 1 + p); //tequila
				else AS = max(0, CharaB->tATK + p);
				if (p != 0) AS = max(1, AS);
				if (CharaA->DEJudge(HP - SpecialDamage_S, AS, _HP - CommonDamage_F) == DEFEND) for(q = CharaA->DEdice.minP; q <= CharaA->DEdice.maxP; q++) //q为A防御骰
				{
					D = max(0, CharaA->tDEF + q);
					if (q != 0) D = max(1, D);
					Damage = max(1, AS - D); 
					CommonDamage_S = max(0, Damage + CharaB->EXD - CharaA->EXR); 
					if (CharaA->passive == 7020) CommonDamage_S = min(2, CommonDamage_S); //miusaki
					SpecialDamage_F = 0; 
					if (SpecialDamage_F != 0) SpecialDamage_F = max(0, SpecialDamage_F + CharaA->EXD - CharaB->EXR); 
					CommonDamage_S = min(HP - SpecialDamage_S, CommonDamage_S);
					SpecialDamage_F = min(_HP - CommonDamage_F, SpecialDamage_F);
					condition_space[HP - CommonDamage_S - SpecialDamage_S][_HP - CommonDamage_F - SpecialDamage_F] += CharaA->Adice.chance[m] * CharaB->DEdice.chance[n] * CharaB->Adice.chance[p] * CharaA->DEdice.chance[q];
					//计算导向这种结果的可能情况数
				}
				else for(q = CharaA->DEdice.minP; q <= CharaA->DEdice.maxP; q++) //q为A闪避骰
				{
					E = max(0, CharaA->tEVD + q);
					if (q != 0) E = max(1, E);
					if (E > AS)
					{
						CommonDamage_S = 0;
						if (CharaA->passive == 7010) SpecialDamage_F = 1; //repa
						else SpecialDamage_F = 0;
						if (SpecialDamage_F != 0) SpecialDamage_F = max(0, SpecialDamage_F + CharaA->EXD - CharaB->EXR);
					}
					else
					{
						Damage = AS;
						CommonDamage_S = max(0, Damage + CharaB->EXD - CharaA->EXR);
						SpecialDamage_F = 0;
						if (SpecialDamage_F != 0) SpecialDamage_F = max(0, SpecialDamage_F + CharaA->EXD - CharaB->EXR);
					}
					CommonDamage_S = min(HP - SpecialDamage_S, CommonDamage_S);
					SpecialDamage_F = min(_HP - CommonDamage_F, SpecialDamage_F);
					condition_space[HP - CommonDamage_S - SpecialDamage_S][_HP - CommonDamage_F - SpecialDamage_F] += CharaA->Adice.chance[m] * CharaB->DEdice.chance[n] * CharaB->Adice.chance[p] * CharaA->DEdice.chance[q];
				} 
			} 
		}
		else for(n = CharaB->DEdice.minP; n <= CharaB->DEdice.maxP; n++) //n为B闪避骰
		{
			E = max(0, CharaB->tEVD + n);
			if (n != 0) E = max(1, E);
			if (E > AF)
			{
				CommonDamage_F = 0; //闪避成功时不受任何伤害
				if (CharaB->passive == 7010) SpecialDamage_S = 1; //repa
				else SpecialDamage_S = 0;
				if (SpecialDamage_S != 0) SpecialDamage_S = max(0, SpecialDamage_S + CharaB->EXD - CharaA->EXR);
			}
			else
			{
				Damage = AF;
				CommonDamage_F = max(0, Damage + CharaA->EXD - CharaB->EXR);
				SpecialDamage_S = 0;
				if (SpecialDamage_S != 0) SpecialDamage_S = max(0, SpecialDamage_S + CharaB->EXD - CharaA->EXR);
			}
			CommonDamage_F = min(_HP, CommonDamage_F);
			SpecialDamage_S = min(HP, SpecialDamage_S);
			if (CommonDamage_F == _HP) condition_space[HP][0] += CharaA->Adice.chance[m] * CharaB->DEdice.chance[n] * dice_condcount_BF;
			else if (SpecialDamage_S == HP) condition_space[0][_HP] += CharaA->Adice.chance[m] * CharaB->DEdice.chance[n] * dice_condcount_BF;
			else for(p = CharaB->Adice.minP; p <= CharaB->Adice.maxP; p++) //这一段与上面的同位段完全相同 
			{
				if (CharaB->passive == 6070) AS = max(0, CharaB->tATK + (CharaB->tHP - _HP + CommonDamage_F) + p);
				else if (CharaB->passive == 6071 && (CharaB->PSIV_TRIG == 1 || (_HP - CommonDamage_F) < CharaB->tHP)) AS = max(0, CharaB->tATK + 1 + p); //tequila
				else AS = max(0, CharaB->tATK + p);
				if (p != 0) AS = max(1, AS);
				if (CharaA->DEJudge(HP - SpecialDamage_S, AS, _HP - CommonDamage_F) == DEFEND) for(q = CharaA->DEdice.minP; q <= CharaA->DEdice.maxP; q++) //q为A防御骰
				{
					D = max(0, CharaA->tDEF + q);
					if (q != 0) D = max(1, D);
					Damage = max(1, AS - D); 
					CommonDamage_S = max(0, Damage + CharaB->EXD - CharaA->EXR); 
					if (CharaA->passive == 7020) CommonDamage_S = min(2, CommonDamage_S); //miusaki
					SpecialDamage_F = 0; 
					if (SpecialDamage_F != 0) SpecialDamage_F = max(0, SpecialDamage_F + CharaA->EXD - CharaB->EXR); 
					CommonDamage_S = min(HP - SpecialDamage_S, CommonDamage_S);
					SpecialDamage_F = min(_HP - CommonDamage_F, SpecialDamage_F);
					condition_space[HP - CommonDamage_S - SpecialDamage_S][_HP - CommonDamage_F - SpecialDamage_F] += CharaA->Adice.chance[m] * CharaB->DEdice.chance[n] * CharaB->Adice.chance[p] * CharaA->DEdice.chance[q];
					//计算导向这种结果的可能情况数
				}
				else for(q = CharaA->DEdice.minP; q <= CharaA->DEdice.maxP; q++) //q为A闪避骰
				{
					E = max(0, CharaA->tEVD + q);
					if (q != 0) E = max(1, E);
					if (E > AS)
					{
						CommonDamage_S = 0;
						if (CharaA->passive == 7010) SpecialDamage_F = 1; //repa
						else SpecialDamage_F = 0;
						if (SpecialDamage_F != 0) SpecialDamage_F = max(0, SpecialDamage_F + CharaA->EXD - CharaB->EXR);
					}
					else
					{
						Damage = AS;
						CommonDamage_S = max(0, Damage + CharaB->EXD - CharaA->EXR);
						SpecialDamage_F = 0;
						if (SpecialDamage_F != 0) SpecialDamage_F = max(0, SpecialDamage_F + CharaA->EXD - CharaB->EXR);
					}
					CommonDamage_S = min(HP - SpecialDamage_S, CommonDamage_S);
					SpecialDamage_F = min(_HP - CommonDamage_F, SpecialDamage_F);
					condition_space[HP - CommonDamage_S - SpecialDamage_S][_HP - CommonDamage_F - SpecialDamage_F] += CharaA->Adice.chance[m] * CharaB->DEdice.chance[n] * CharaB->Adice.chance[p] * CharaA->DEdice.chance[q];
				} 
			} 
		} 
	}
}

float Get_Winrate(Character *CharaA, Character *CharaB)
{
	int i, j, s, t;
	int sp_size[3]; 
	int dice_condcount_AF, dice_condcount_BF;
	float ChangeRate;
	float tot;
	sp_size[1] = CharaA->HP;
	sp_size[2] = CharaB->HP;
	memset(winrate_space, 0.0, sizeof(winrate_space));
	for(i = 1; i <= sp_size[1]; i++) winrate_space[i][0] = 100.0;
	for(j = 1; j <= sp_size[2]; j++) winrate_space[0][j] = 0.0; //胜率空间的HPB=0行表示A胜，将胜率置为100，HPA=0行将胜率置为0
	dice_condcount_AF = CharaA->Adice.condcount * CharaB->DEdice.condcount;
	dice_condcount_BF = CharaB->Adice.condcount * CharaA->DEdice.condcount; //计算一个攻防的骰子情况数

	for(i = 1; i <= sp_size[1]; i++)
	for(j = 1; j <= sp_size[2]; j++) //i为charaA血量，j为charaB血量 
	{
		Get_CondSpace(CharaA, CharaB, i, j, dice_condcount_BF);
		ChangeRate = 1 - (1.0 * condition_space[i][j] / dice_condcount_AF / dice_condcount_BF); //计算状态空间改变的几率 
		tot = 0.0;
		for(s = 0; s <= i; s++) for(t = 0; t <= j; t++) tot += winrate_space[s][t] * condition_space[s][t]; //计算胜率和 
		winrate_space[i][j] = tot / (dice_condcount_AF * dice_condcount_BF) / ChangeRate;
	}
	return winrate_space[CharaA->HP][CharaB->HP];
}

float Get_Strategy(Character *CharaA, Character *CharaB)
{
	int NE = 0;
	float WR, tempWR;
	int t = 1;
	int i, j, k;
	while (NE != 1 && t <= ITERATETIME) //若纳什均衡达成或迭代次数过多（用以截断最佳策略循环），跳出循环
	{
		WR = Get_Winrate(CharaA, CharaB); //WR为这一次策略改变前CharaA的基础胜率
		NE = 1;
		for(i = 1; i <= CharaA->str_size[1]; i++)
		for(j = 1; j <= CharaA->str_size[2]; j++)
		for(k = 1; k <= CharaA->str_size[3]; k++)
		{
			CharaA->toggleDE(i, j, k); //更改CharaA的一个策略
			tempWR = Get_Winrate(CharaA, CharaB);
			if (tempWR > WR)
			{
				WR = tempWR;
				NE = 0; //如果CharaA胜率提升，保留策略更改并覆写WR，有策略发生变动时，代表纳什均衡未达成
			}
			else
			{
				if (fabs(tempWR - WR) < 1e-9) CharaA->unused_strategy[i][j][k] = 1;
				else CharaA->unused_strategy[i][j][k] = 0; //标记对胜率无影响的策略
				CharaA->toggleDE(i, j, k); //回退策略 
			}
		}
		for(i = 1; i <= CharaB->str_size[1]; i++)
		for(j = 1; j <= CharaB->str_size[2]; j++)
		for(k = 1; k <= CharaB->str_size[3]; k++)
		{
			CharaB->toggleDE(i, j, k);
			tempWR = Get_Winrate(CharaA, CharaB);
			if (tempWR < WR)
			{
				WR = tempWR;
				NE = 0; //如果CharaA胜率下降（等效于CharaB胜率上升），保留策略更改并覆写WR
			}
			else
			{
				if (fabs(tempWR - WR) < 1e-9) CharaB->unused_strategy[i][j][k] = 1;
				else CharaB->unused_strategy[i][j][k] = 0; 
				CharaB->toggleDE(i, j, k);
			}
		}
		//printf("#Strategy Iteration:%d Time(s)\n", t);
		t++; //增加迭代计数器 
	}
	//printf("#Iteration Done.\n");
	return WR;
}

void Final_Battle(Character *CharaA, Character *CharaB, int turn)
{
	int dice_condcount_AF, dice_condcount_BF;
	int t, i, j, m, n;
	
	memset(fb_winrate_space, 0.0, sizeof(fb_winrate_space));
	rate_Win = 0;
	rate_Lose = 0;
	rate_Draw = 0;
	dice_condcount_AF = CharaA->Adice.condcount * CharaB->DEdice.condcount;
	dice_condcount_BF = CharaB->Adice.condcount * CharaA->DEdice.condcount;
	fb_winrate_space[0][CharaA->HP][CharaB->HP] = 100.0; //初始状态，0t时双方血量为满 
	for(t = 0; t < turn; t++)
	{
		for(i = 0; i <= CharaA->HP; i++)
		for(j = 0; j <= CharaB->HP; j++) if (fabs(fb_winrate_space[t][i][j]) > 1e-9) //概率为0时不进行处理 
		{
			if (i == 0 || j == 0) fb_winrate_space[t + 1][i][j] += fb_winrate_space[t][i][j]; //其中一个血量为0时，直接简化处理 
			else
			{
				Get_CondSpace(CharaA, CharaB, i, j, dice_condcount_BF);
				for (m = 0; m <= i; m++)
				for (n = 0; n <= j; n++)
				fb_winrate_space[t + 1][m][n] += fb_winrate_space[t][i][j] * condition_space[m][n] / (dice_condcount_AF * dice_condcount_BF); 
				//这里的winratespace存储在t回合(HPA=i, HPB=j)的概率 
			}
		}
		//printf("#Simulate Battle:%d/%d Turns\n", t+1, turn);
	}
	for(i = 1; i <= CharaA->HP; i++) rate_Win += fb_winrate_space[turn][i][0];
	for(j = 1; j <= CharaB->HP; j++) rate_Lose += fb_winrate_space[turn][0][j];
	rate_Draw = 100.0 - rate_Win - rate_Lose;
	//printf("#%.3lf%%\n", fb_winrate_space[turn][CharaA->HP][0]);
}

void Sim_Battle(Character *CharaA, Character *CharaB, int turn)
{
	
}

void Start_Simulate(Character *CharaA, Character *CharaB)
{
	int turn = 10;
	if (CharaA->passive == 8020 || CharaB->passive == 8020) turn = 3;
	CharaA->Reset_Temp(1, CharaB);
	CharaB->Reset_Temp(2, CharaA); //重设临时属性，后续使用临时属性进行运算避免影响原属性
	//这里是用卡
	 
	//这里是用卡
	//雪梨自己把先后手给我写好咯 这玩意我是不想处理了 谁爱做谁做
	if (CharaA->passive_depth + CharaB->passive_depth > 0)
	{
		printf("OvO\n");
		/*int t;
		int winC, loseC, drawC; 
		winC = 0;
		loseC = 0;
		drawC = 0; 
		for(t = 1; t <= SIMTIME; t++)
		{
			solu = SimBattle(CharaA, CharaB, turn)
		}*/
	}
	else
	{
		//printf("#2\n");
		CharaA->Strategy_Build(CharaB->tHP);
		CharaB->Strategy_Build(CharaA->tHP);
		printf("Infturn Winrate: %.3f%%\n",Get_Strategy(CharaA, CharaB));
		Final_Battle(CharaA, CharaB, turn);
		printf("%s VS %s:\n", CharaA->name, CharaB->name);
		printf("Win: %.3f%%\tLose:%.3f%%\tDraw:%.3f%%\n", rate_Win, rate_Lose, rate_Draw);
		printf("%s(P1): BR=\n", CharaA->name);
		CharaA->Print_Strategy();
		printf("%s(P2): BR=\n", CharaB->name);
		CharaB->Print_Strategy();
	}
}

int main(int argc, char* argv[])
{
	int i, j, n, nul;
	char command[STRINGLENGTH], aname[STRINGLENGTH], bname[STRINGLENGTH];
	int id1, id2, value, target;
	int map[1000], mapcount; 
	int search1, search2;
	char space;
	DiceReset();
	FILE *input;
	input = fopen("CharacterData.txt", "r");
	i = 1;
	while (fscanf(input, "%d", &n) != EOF)
		{
			fscanf(input, "%s%d%d%d%d%d", &CommonChar[n].name, &CommonChar[n].MHP, &CommonChar[n].ATK, &CommonChar[n].DEF, &CommonChar[n].EVD, &CommonChar[n].passive);
			map[i] = n;
			i++;
		}
	mapcount = i - 1;
	fclose(input); 
	// 读入数据库 
		if (argc == 4) //快速对战 
		{
			strcpy(aname, argv[2]);
			strcpy(bname, argv[3]);
			search1 = 0;
			search2 = 0;
			for(i = 1; i <= mapcount; i++) 
			{
				if (strcmp(CommonChar[map[i]].name, aname) == 0) 
				{
					search1 = 1;
					id1 = map[i];
				}
				if (strcmp(CommonChar[map[i]].name, bname) == 0)
				{
					search2 = 1;
					id2 = map[i];
				}
			}
			if (search1 + search2 != 2)
			{		 
				printf("#Parameter Error\n");
				goto done;
			}
			CharaA.Load_CharaData(CommonChar[id1]);
			CharaB.Load_CharaData(CommonChar[id2]);
			Start_Simulate(&CharaA, &CharaB);
		}
		else if (argc == 20) //设置骰子
		{
			char psiv1[STRINGLENGTH], psiv2[STRINGLENGTH];
			char dicename1[STRINGLENGTH], dicename2[STRINGLENGTH];
			int a1, a2, a3, a4, a5, a6, b1, b2, b3, b4, b5, b6, apsiv, bpsiv;
			strcpy(aname, argv[2]);
			a1 = atoi(argv[3]);
			a2 = atoi(argv[4]);
			a3 = atoi(argv[5]);
			a4 = atoi(argv[6]);
			strcpy(psiv1, argv[7]);
			a5 = atoi(argv[8]);
			a6 = atoi(argv[9]);
			strcpy(dicename1, argv[10]);
			strcpy(bname, argv[11]);
			b1 = atoi(argv[12]);
			b2 = atoi(argv[13]);
			b3 = atoi(argv[14]);
			b4 = atoi(argv[15]);
			strcpy(psiv2, argv[16]);
			b5 = atoi(argv[17]);
			b6 = atoi(argv[18]);
			strcpy(dicename2, argv[19]);
			CharaA.Load_CharaData(CommonChar[0]);
			CharaB.Load_CharaData(CommonChar[0]);
			strcpy(CharaA.name, aname);
			CharaA.Set_Stat(a1, a2, a3, a4);
			CharaA.Edit_Stat("EXD", a5);
			CharaA.Edit_Stat("EXR", a6);
			CharaA.Set_Dice(dicename1);
			strcpy(CharaB.name, bname);
			CharaB.Set_Stat(b1, b2, b3, b4);
			CharaB.Edit_Stat("EXD", b5);
			CharaB.Edit_Stat("EXR", b6);
			CharaB.Set_Dice(dicename2);
			for(i = 1; i <= mapcount; i++)
			{
				if (strcmp(CommonChar[map[i]].name, psiv1) == 0) CharaA.Set_Passive(CommonChar[map[i]].passive);
				if (strcmp(CommonChar[map[i]].name, psiv2) == 0) CharaB.Set_Passive(CommonChar[map[i]].passive);
			}
			//CharaA.Show_Data();
			//CharaB.Show_Data();
			Start_Simulate(&CharaA, &CharaB);
		}
		done: nul = 0;
	return 0;
 } 
