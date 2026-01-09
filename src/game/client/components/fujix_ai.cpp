#include <engine/shared/config.h>
#include <game/client/gameclient.h>
#include <game/client/components/controls.h>
#include <game/collision.h>
#include "fujix_ai.h"

#include <queue>
#include <map>
#include <algorithm>

void CFujixAI::OnInit()
{
	m_PathIndex = 0;
	m_LastPathFindTime = 0;
	
	// Initialize prediction core
	m_PredictWorld.InitSwitchers(0); // Basic init
	m_PredictCore.Init(&m_PredictWorld, GameClient()->Collision());
}

void CFujixAI::OnRender()
{
	if(!g_Config.m_ClFujixEnable)
		return;

	if(Client()->State() != IClient::STATE_ONLINE)
		return;

	UpdateControls();
}

vec2 CFujixAI::PredictPos(vec2 Pos, vec2 Vel, int Direction, int Ticks)
{
	// Setup core for prediction
	m_PredictCore.Reset();
	m_PredictCore.m_Pos = Pos;
	m_PredictCore.m_Vel = Vel;
	// Sync turning params if needed, for now assume default or copied
	m_PredictCore.m_Tuning = GameClient()->m_aTuning[g_Config.m_ClDummy];
	
	// Simulate
	for(int i = 0; i < Ticks; i++)
	{
		m_PredictCore.m_Input.m_Direction = Direction;
		m_PredictCore.Tick(true);
		m_PredictCore.Move();
	}
	
	return m_PredictCore.m_Pos;
}

bool CFujixAI::IsSolid(int x, int y) const
{
	if(x < 0 || x >= GameClient()->Collision()->GetWidth() || y < 0 || y >= GameClient()->Collision()->GetHeight())
		return true;
	return GameClient()->Collision()->IsSolid(x, y);
}

void CFujixAI::FindPath(vec2 Start, vec2 End)
{
	m_Path.clear();
	m_PathIndex = 0;

	int StartX = round_to_int(Start.x / 32.0f);
	int StartY = round_to_int(Start.y / 32.0f);
	int EndX = round_to_int(End.x / 32.0f);
	int EndY = round_to_int(End.y / 32.0f);

	if(IsSolid(StartX, StartY)) // Try to find nearest non-solid if stuck
	{
		// Simple wiggle to find air
		if(!IsSolid(StartX, StartY - 1)) StartY--;
		else if(!IsSolid(StartX, StartY + 1)) StartY++;
	}
	
	if (StartX == EndX && StartY == EndY) return;

	std::priority_queue<std::pair<int, AStarNode*>, std::vector<std::pair<int, AStarNode*>>, std::greater<std::pair<int, AStarNode*>>> OpenList;
	std::map<std::pair<int, int>, AStarNode*> AllNodes;

	AStarNode *StartNode = new AStarNode{StartX, StartY, 0, abs(EndX - StartX) + abs(EndY - StartY), nullptr};
	OpenList.push({StartNode->F(), StartNode});
	AllNodes[{StartX, StartY}] = StartNode;

	// Limit search space
	int Iterations = 0;
	const int MaxIterations = 2000; 

	while(!OpenList.empty() && Iterations < MaxIterations)
	{
		AStarNode *Current = OpenList.top().second;
		OpenList.pop();

		Iterations++;

		if(Current->X == EndX && Current->Y == EndY)
		{
			// Path found
			AStarNode *PathNode = Current;
			while(PathNode)
			{
				m_Path.push_back(vec2(PathNode->X * 32.0f + 16.0f, PathNode->Y * 32.0f + 16.0f));
				PathNode = PathNode->pParent;
			}
			std::reverse(m_Path.begin(), m_Path.end());
			break;
		}

		// Neighbors: Up, Down, Left, Right, Diagonals
		int dx[] = {0, 0, -1, 1, -1, 1, -1, 1};
		int dy[] = {-1, 1, 0, 0, -1, -1, 1, 1};

		for(int i = 0; i < 8; i++)
		{
			int NewX = Current->X + dx[i];
			int NewY = Current->Y + dy[i];

			if(IsSolid(NewX, NewY)) continue;

			// Movement cost
			int G = Current->G + (i < 4 ? 10 : 14); 

			// Check if already visited with lower cost
			if(AllNodes.count({NewX, NewY}))
			{
				if(AllNodes[{NewX, NewY}]->G <= G) continue;
			}

			AStarNode *NewNode = new AStarNode{NewX, NewY, G, abs(EndX - NewX) + abs(EndY - NewY), Current};
			AllNodes[{NewX, NewY}] = NewNode;
			OpenList.push({NewNode->F(), NewNode});
		}
	}

	// Cleanup
	for(auto const& [key, val] : AllNodes)
	{
		if(val != nullptr)
			delete val;
	}
}

void CFujixAI::UpdateHook(CControls *pControls)
{
	if(m_Path.empty()) return;

	int Dummy = g_Config.m_ClDummy;
	vec2 CharPos = GameClient()->m_LocalCharacterPos;
	
	// Find a hook target (simple search for solid blocks above/ahead)
	vec2 SearchDir = normalize(vec2(pControls->m_aInputData[Dummy].m_Direction * 100.0f, -100.0f));
	vec2 HookTarget = CharPos + SearchDir * 300.0f;
	
	// Check if we can hook something useful
	if(GameClient()->Collision()->IntersectLineTeleHook(CharPos, HookTarget, &HookTarget, nullptr, nullptr))
	{
		// Simulate hook pull
		vec2 PredictedPos = PredictPos(CharPos, GameClient()->m_PredictedChar.m_Vel, pControls->m_aInputData[Dummy].m_Direction, 10);
		
		// If prediction says we are falling too much, hook!
		if(PredictedPos.y > CharPos.y + 50.0f)
		{
			pControls->m_aInputData[Dummy].m_Hook = 1;
			pControls->m_aInputData[Dummy].m_TargetX = HookTarget.x - CharPos.x;
			pControls->m_aInputData[Dummy].m_TargetY = HookTarget.y - CharPos.y;
		}
	}
}

void CFujixAI::UpdateControls()
{
	CControls *pControls = &GameClient()->m_Controls;
	int Dummy = g_Config.m_ClDummy;
	vec2 CharPos = GameClient()->m_LocalCharacterPos;
	vec2 CharVel = GameClient()->m_PredictedChar.m_Vel;

	// Reset inputs
	pControls->m_aInputData[Dummy].m_Direction = 0;
	pControls->m_aInputData[Dummy].m_Jump = 0;
	pControls->m_aInputData[Dummy].m_Hook = 0;

	// 1. Goal Selection (Move right for now)
	vec2 TargetPos = CharPos + vec2(800.0f, 0.0f); // Look ahead
	// Clamp goal to world bounds (rough)
	if(TargetPos.x > GameClient()->Collision()->GetWidth() * 32.0f) 
		TargetPos.x = GameClient()->Collision()->GetWidth() * 32.0f - 32.0f;

	// 2. Pathfinding Update
	int64_t Now = time_get();
	if(Now - m_LastPathFindTime > time_freq() * 0.5f || m_Path.empty() || m_PathIndex >= m_Path.size()) // Every 0.5s or if finished
	{
		FindPath(CharPos, TargetPos);
		m_LastPathFindTime = Now;
	}

	// 3. Path Following with Prediction
	if(m_Path.empty()) return;

	// Find close point on path
	vec2 NextPoint = m_Path[std::min((size_t)m_PathIndex + 5, m_Path.size() - 1)]; // Look further ahead
	float Distance = distance(CharPos, NextPoint);
	
	if(Distance < 64.0f && m_PathIndex < m_Path.size() - 1)
	{
		m_PathIndex++;
	}

	// Steering based on prediction
	vec2 PredictedPos = PredictPos(CharPos, CharVel, 1, 10);
	vec2 PredictedPosLeft = PredictPos(CharPos, CharVel, -1, 10);
	
	float DistRight = distance(PredictedPos, NextPoint);
	float DistLeft = distance(PredictedPosLeft, NextPoint);
	
	if(DistRight < DistLeft) pControls->m_aInputData[Dummy].m_Direction = 1;
	else pControls->m_aInputData[Dummy].m_Direction = -1;

	// Jump if needed (if next point is higher and prediction says we won't make it)
	if(NextPoint.y < CharPos.y - 10.0f) 
	{
		vec2 PredJump = PredictPos(CharPos, vec2(CharVel.x, -10.0f), pControls->m_aInputData[Dummy].m_Direction, 20); // Simulate jump
		if(PredJump.y < CharPos.y) // If jump takes us up
			pControls->m_aInputData[Dummy].m_Jump = 1;
	}
	
	UpdateHook(pControls);
}
